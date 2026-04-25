CREATE EXTENSION IF NOT EXISTS vector;

CREATE TYPE public."comment_visibility" AS ENUM (
	'PUBLIC',
	'INTERNAL');

CREATE TYPE public."ticket_priority" AS ENUM (
	'LOW',
	'MEDIUM',
	'HIGH',
	'URGENT');

CREATE TYPE public."ticket_status" AS ENUM (
	'NEW',
	'AI_PROCESSING',
	'VAGUE',
	'READY',
	'IN_PROGRESS',
	'DUPLICATE',
	'RESOLVED',
	'CLOSED',
	'DUPLICATE_REVIEW',
	'KB_SUGGESTED');

CREATE TYPE public."user_role" AS ENUM (
	'USER',
	'AGENT',
	'ADMIN');

CREATE TABLE public.outbox_events (
	oe_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	oe_event_type varchar(60) NOT NULL,
	oe_aggregate_type varchar(40) NOT NULL,
	oe_aggregate_id int8 NOT NULL,
	oe_payload jsonb DEFAULT '{}'::jsonb NOT NULL,
	oe_status varchar(20) NOT NULL,
	oe_retry_count int4 DEFAULT 0 NOT NULL,
	oe_next_run_at timestamptz NULL,
	oe_last_error text NULL,
	oe_created_at timestamptz DEFAULT now() NOT NULL,
	oe_processed_at timestamptz NULL,
	CONSTRAINT outbox_events_aggregate_type_check CHECK (((oe_aggregate_type)::text = ANY ((ARRAY['TICKET'::character varying, 'KB_ARTICLE'::character varying])::text[]))),
	CONSTRAINT outbox_events_check CHECK (((oe_status)::text = ANY ((ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'DONE'::character varying, 'FAILED'::character varying])::text[]))),
	CONSTRAINT outbox_events_event_type_check CHECK (((oe_event_type)::text = ANY ((ARRAY['TRIAGE_REQUESTED'::character varying, 'ROUTING_REQUESTED'::character varying, 'DUPLICATE_CHECK_REQUESTED'::character varying, 'VAGUE_CHECK_REQUESTED'::character varying, 'KB_SUGGESTION_REQUESTED'::character varying, 'KB_DRAFT_REQUESTED'::character varying])::text[]))),
	CONSTRAINT outbox_events_pk PRIMARY KEY (oe_id)
);
CREATE INDEX outbox_events_oe_status_idx ON public.outbox_events USING btree (oe_status, oe_next_run_at, oe_created_at);

CREATE TABLE public.users (
	user_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	user_email_id varchar NOT NULL,
	user_name varchar(35) NOT NULL,
	"user_role" public."user_role" NOT NULL,
	user_created_at timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
	user_password varchar(255) NULL,
	user_department varchar(80) NULL,
	CONSTRAINT email_format_check CHECK (((user_email_id)::text ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'::text)),
	CONSTRAINT u_email_id_unique UNIQUE NULLS NOT DISTINCT (user_email_id),
	CONSTRAINT users_department_check CHECK (((user_department IS NULL) OR ((user_department)::text = ANY ((ARRAY['TECHNICAL SUPPORT'::character varying, 'BILLING AND PAYMENTS'::character varying, 'ORDERS AND RETURNS'::character varying, 'SALES AND PRESALES'::character varying, 'ACCOUNT AND ACCESS'::character varying, 'GENERAL INQUIRY'::character varying])::text[])))),
	CONSTRAINT users_pkey PRIMARY KEY (user_id)
);


-- public.refresh_tokens definition

-- Drop table

-- DROP TABLE public.refresh_tokens;

CREATE TABLE public.refresh_tokens (
	rt_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	rt_user_id int8 NOT NULL,
	rt_token_hash varchar(255) NOT NULL,
	rt_expires_at timestamptz NOT NULL,
	rt_revoked bool DEFAULT false NOT NULL,
	rt_created_at timestamptz DEFAULT now() NOT NULL,
	rt_updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT refresh_tokens_pk PRIMARY KEY (rt_id),
	CONSTRAINT refresh_tokens_unique UNIQUE (rt_user_id),
	CONSTRAINT refresh_tokens_users_fk FOREIGN KEY (rt_user_id) REFERENCES public.users(user_id) ON DELETE CASCADE
);

CREATE TABLE public.ticket_text_versions (
	ttv_version_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	ttv_ticket_id int8 NOT NULL,
	ttv_version_no int4 NOT NULL,
	ttv_ticket_title varchar(200) NOT NULL,
	ttv_ticket_description text NOT NULL,
	ttv_ticket_created_by int8 NOT NULL,
	ttv_ticket_created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT ticket_text_versions_pk PRIMARY KEY (ttv_version_id),
	CONSTRAINT ticket_text_versions_unique UNIQUE (ttv_ticket_id, ttv_version_no),
	CONSTRAINT ticket_text_versions_users_fk FOREIGN KEY (ttv_ticket_created_by) REFERENCES public.users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE INDEX ticket_text_versions_ttv_ticket_id_idx ON public.ticket_text_versions USING btree (ttv_ticket_id);

CREATE TABLE public.tickets (
	ticket_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	ticket_title varchar(200) NOT NULL,
	ticket_description text NOT NULL,
	"ticket_status" public."ticket_status" NOT NULL,
	ticket_created_by int8 NOT NULL,
	ticket_assigned_to int8 NULL,
	ticket_ai_category varchar(80) NULL,
	ticket_ai_priority public."ticket_priority" NULL,
	ticket_created_at timestamptz DEFAULT now() NOT NULL,
	ticket_updated_at timestamptz DEFAULT now() NOT NULL,
	ticket_ai_confidence numeric(4, 3) NULL,
	ticket_current_text_version int4 DEFAULT 1 NOT NULL,
	ticket_duplicate_state varchar(20) DEFAULT 'NONE'::character varying NOT NULL,
	ticket_ai_failed bool DEFAULT false NOT NULL,
	ticket_ai_last_error text NULL,
	ticket_vague_reason text NULL,
	ticket_clarification_prompt text NULL,
	ticket_vague_count int4 DEFAULT 0 NOT NULL,
	ticket_last_vague_at timestamptz NULL,
	ticket_ai_triaged_at timestamptz NULL,
	ticket_first_assigned_at timestamptz NULL,
	ticket_current_triage_started_at timestamptz NULL,
	ticket_current_duplicate_check_started_at timestamptz NULL,
	ticket_duplicate_checked_at timestamptz NULL,
	CONSTRAINT tickets_ai_confidence_check CHECK (((ticket_ai_confidence IS NULL) OR ((ticket_ai_confidence >= (0)::numeric) AND (ticket_ai_confidence <= (1)::numeric)))),
	CONSTRAINT tickets_pk PRIMARY KEY (ticket_id),
	CONSTRAINT tickets_ticket_duplicate_state_check CHECK (((ticket_duplicate_state)::text = ANY ((ARRAY['NONE'::character varying, 'POTENTIAL'::character varying, 'CONFIRMED'::character varying])::text[]))),
	CONSTRAINT ticket_assigned_to_user_fk FOREIGN KEY (ticket_assigned_to) REFERENCES public.users(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
	CONSTRAINT tickets_created_by_user_fk FOREIGN KEY (ticket_created_by) REFERENCES public.users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE INDEX tickets_ticket_assigned_to_idx ON public.tickets USING btree (ticket_assigned_to);
CREATE INDEX tickets_ticket_created_at_idx ON public.tickets USING btree (ticket_created_at);
CREATE INDEX tickets_ticket_created_by_idx ON public.tickets USING btree (ticket_created_by);
CREATE INDEX tickets_ticket_status_idx ON public.tickets USING btree (ticket_status);

CREATE TABLE public.admin_overrides (
	ao_override_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	ao_ticket_id int8 NOT NULL,
	ao_overridden_by int8 NOT NULL,
	ao_override_type varchar(40) NOT NULL,
	ao_old_value text NULL,
	ao_new_value text NULL,
	ao_reason text NULL,
	ao_created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT admin_overrides_check CHECK (((ao_override_type)::text = ANY (ARRAY[('CATEGORY'::character varying)::text, ('PRIORITY'::character varying)::text, ('DUPLICATE_LINK'::character varying)::text, ('STATUS'::character varying)::text, ('KB_DRAFT'::character varying)::text, ('ASSIGNMENT'::character varying)::text]))),
	CONSTRAINT admin_overrides_pk PRIMARY KEY (ao_override_id),
	CONSTRAINT admin_overrides_tickets_fk FOREIGN KEY (ao_ticket_id) REFERENCES public.tickets(ticket_id) ON DELETE CASCADE,
	CONSTRAINT admin_overrides_users_fk FOREIGN KEY (ao_overridden_by) REFERENCES public.users(user_id)
);
CREATE INDEX admin_overrides_ao_ticket_id_idx ON public.admin_overrides USING btree (ao_ticket_id, ao_created_at DESC);

CREATE TABLE public.ai_decisions (
	ai_decision_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	ad_ticket_id int8 NOT NULL,
	ad_text_version int4 NOT NULL,
	ad_decision_type varchar(40) NOT NULL,
	ad_output_json jsonb DEFAULT '{}'::jsonb NOT NULL,
	ad_confidence numeric(4, 3) NULL,
	ad_similarity numeric(6, 5) NULL,
	ad_threshold numeric(6, 5) NULL,
	ad_created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT ai_decisions_check CHECK (((ad_decision_type)::text = ANY ((ARRAY['CLASSIFICATION'::character varying, 'PRIORITY'::character varying, 'ROUTING'::character varying, 'VAGUE_CHECK'::character varying, 'DUPLICATE_CHECK'::character varying, 'KB_SUGGESTION'::character varying, 'KB_DRAFT'::character varying])::text[]))),
	CONSTRAINT ai_decisions_check_1 CHECK (((ad_confidence IS NULL) OR ((ad_confidence >= (0)::numeric) AND (ad_confidence <= (1)::numeric)))),
	CONSTRAINT ai_decisions_pk PRIMARY KEY (ai_decision_id),
	CONSTRAINT ai_decisions_ticket_text_versions_fk FOREIGN KEY (ad_ticket_id,ad_text_version) REFERENCES public.ticket_text_versions(ttv_ticket_id,ttv_version_no),
	CONSTRAINT ai_decisions_tickets_fk FOREIGN KEY (ad_ticket_id) REFERENCES public.tickets(ticket_id) ON DELETE CASCADE
);
CREATE INDEX ai_decisions_ad_ticket_id_idx ON public.ai_decisions USING btree (ad_ticket_id, ad_created_at DESC);

CREATE TABLE public.kb_articles (
	kba_kb_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	kba_title varchar(200) NOT NULL,
	kba_body text NOT NULL,
	kba_status varchar(20) NOT NULL,
	kba_created_by int8 NOT NULL,
	kba_source_ticket_id int8 NULL,
	kba_created_at timestamptz DEFAULT now() NOT NULL,
	kba_updated_at timestamptz DEFAULT now() NOT NULL,
	kba_approved_at timestamptz NULL,
	kba_last_modified_by int8 NULL,
	kba_is_ai_generated bool DEFAULT false NOT NULL,
	kba_approved_by int8 NULL,
	kba_agent_submitted_at timestamptz NULL,
	CONSTRAINT kb_articles_check CHECK (((kba_status)::text = ANY (ARRAY[('DRAFT'::character varying)::text, ('IN_REVIEW'::character varying)::text, ('PUBLISHED'::character varying)::text, ('REJECTED'::character varying)::text]))),
	CONSTRAINT kb_articles_pk PRIMARY KEY (kba_kb_id),
	CONSTRAINT kb_articles_approved_by_fk FOREIGN KEY (kba_approved_by) REFERENCES public.users(user_id),
	CONSTRAINT kb_articles_last_modified_by_fk FOREIGN KEY (kba_last_modified_by) REFERENCES public.users(user_id),
	CONSTRAINT kb_articles_tickets_fk FOREIGN KEY (kba_source_ticket_id) REFERENCES public.tickets(ticket_id),
	CONSTRAINT kb_articles_users_fk FOREIGN KEY (kba_created_by) REFERENCES public.users(user_id)
);
CREATE INDEX kb_articles_kba_status_idx ON public.kb_articles USING btree (kba_status);

CREATE TABLE public.kb_embeddings (
	kbe_kb_id int8 NOT NULL,
	kbe_embedding vector(1536) NOT NULL,
	kbe_created_at timestamptz DEFAULT now() NOT NULL,
	kbe_updated_at timestamptz NULL,
	CONSTRAINT kb_embeddings_pk PRIMARY KEY (kbe_kb_id),
	CONSTRAINT kb_embeddings_kb_articles_fk FOREIGN KEY (kbe_kb_id) REFERENCES public.kb_articles(kba_kb_id) ON DELETE CASCADE
);
CREATE INDEX kb_embeddings_hnsw_idx ON public.kb_embeddings USING hnsw (kbe_embedding vector_cosine_ops);

CREATE TABLE public.kb_suggestions (
	kbs_suggestion_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	kbs_ticket_id int8 NOT NULL,
	kbs_kb_id int8 NOT NULL,
	kbs_similarity numeric(6, 5) NULL,
	kbs_created_at timestamptz DEFAULT now() NOT NULL,
	kbs_source varchar(20) NOT NULL,
	kbs_status varchar(20) NOT NULL,
	kbs_responded_at timestamptz NULL,
	CONSTRAINT kb_suggestions_pk PRIMARY KEY (kbs_suggestion_id),
	CONSTRAINT kb_suggestions_source_check CHECK (((kbs_source)::text = ANY (ARRAY[('AI'::character varying)::text, ('MANUAL_AGENT'::character varying)::text]))),
	CONSTRAINT kb_suggestions_status_check CHECK (((kbs_status)::text = ANY (ARRAY[('SUGGESTED'::character varying)::text, ('ACCEPTED'::character varying)::text, ('REJECTED'::character varying)::text]))),
	CONSTRAINT kb_suggestions_unique UNIQUE (kbs_ticket_id, kbs_kb_id),
	CONSTRAINT kb_suggestions_kb_articles_fk FOREIGN KEY (kbs_kb_id) REFERENCES public.kb_articles(kba_kb_id) ON DELETE CASCADE,
	CONSTRAINT kb_suggestions_tickets_fk FOREIGN KEY (kbs_ticket_id) REFERENCES public.tickets(ticket_id) ON DELETE CASCADE
);
CREATE INDEX kb_suggestions_kbs_ticket_id_idx ON public.kb_suggestions USING btree (kbs_ticket_id);

CREATE TABLE public.ticket_comments (
	tc_comment_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	tc_ticket_id int8 NOT NULL,
	tc_author_id int8 NOT NULL,
	tc_body text NOT NULL,
	tc_created_at timestamptz DEFAULT now() NOT NULL,
	tc_visibility public."comment_visibility" DEFAULT 'PUBLIC'::comment_visibility NOT NULL,
	CONSTRAINT ticket_comments_pk PRIMARY KEY (tc_comment_id),
	CONSTRAINT ticket_comments_tickets_fk FOREIGN KEY (tc_ticket_id) REFERENCES public.tickets(ticket_id) ON DELETE CASCADE,
	CONSTRAINT ticket_comments_users_fk FOREIGN KEY (tc_author_id) REFERENCES public.users(user_id)
);
CREATE INDEX ticket_comments_tc_ticket_id_idx ON public.ticket_comments USING btree (tc_ticket_id);

CREATE TABLE public.ticket_duplicate_links (
	tdl_link_id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
	tdl_primary_ticket_id int8 NOT NULL,
	tdl_duplicate_ticket_id int8 NOT NULL,
	tdl_similarity numeric(6, 5) NULL,
	tdl_created_at timestamptz DEFAULT now() NOT NULL,
	tdl_duplicate_type varchar(20) DEFAULT 'POTENTIAL'::character varying NOT NULL,
	tdl_link_status varchar(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
	tdl_propagate_resolution bool DEFAULT false NOT NULL,
	CONSTRAINT ticket_duplicate_links_check CHECK ((tdl_primary_ticket_id <> tdl_duplicate_ticket_id)),
	CONSTRAINT ticket_duplicate_links_pk PRIMARY KEY (tdl_link_id),
	CONSTRAINT ticket_duplicate_links_tdl_duplicate_type_check CHECK (((tdl_duplicate_type)::text = ANY ((ARRAY['POTENTIAL'::character varying, 'CONFIRMED'::character varying])::text[]))),
	CONSTRAINT ticket_duplicate_links_tdl_link_status_check CHECK (((tdl_link_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'REJECTED'::character varying])::text[]))),
	CONSTRAINT ticket_duplicate_links_unique UNIQUE (tdl_primary_ticket_id, tdl_duplicate_ticket_id),
	CONSTRAINT ticket_duplicate_links_tickets_fk FOREIGN KEY (tdl_primary_ticket_id) REFERENCES public.tickets(ticket_id) ON DELETE CASCADE,
	CONSTRAINT ticket_duplicate_links_tickets_fk_1 FOREIGN KEY (tdl_duplicate_ticket_id) REFERENCES public.tickets(ticket_id) ON DELETE CASCADE
);
CREATE INDEX ticket_duplicate_links_duplicate_idx ON public.ticket_duplicate_links USING btree (tdl_duplicate_ticket_id, tdl_link_status);
CREATE INDEX ticket_duplicate_links_primary_idx ON public.ticket_duplicate_links USING btree (tdl_primary_ticket_id, tdl_link_status);

CREATE TABLE public.ticket_embeddings (
	te_ticket_id int8 NOT NULL,
	te_text_version int4 NOT NULL,
	te_embedding vector(1536) NOT NULL,
	te_created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT ticket_embeddings_pk PRIMARY KEY (te_ticket_id, te_text_version),
	CONSTRAINT ticket_embeddings_tickets_fk FOREIGN KEY (te_ticket_id) REFERENCES public.tickets(ticket_id) ON DELETE CASCADE
);
CREATE INDEX ticket_embeddings_hnsw_idx ON public.ticket_embeddings USING hnsw (te_embedding vector_cosine_ops);