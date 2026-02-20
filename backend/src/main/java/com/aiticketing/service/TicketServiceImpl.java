package com.aiticketing.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiticketing.bean.request.CreateTicketRequestBean;
import com.aiticketing.bean.response.TicketResponseBean;
import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.TicketStatus;
import com.aiticketing.entity.User;
import com.aiticketing.exception.NotFoundException;
import com.aiticketing.repository.TicketRepository;
import com.aiticketing.repository.UserRepository;

@Service("TicketServiceImpl")
public class TicketServiceImpl implements TicketService {

	@Autowired
	TicketRepository ticketRepo;

	@Autowired
	UserRepository userRepo;

	private static final Logger TICKET_SERVICE_LOG = LoggerFactory.getLogger(TicketServiceImpl.class);

	@Transactional
	public TicketResponseBean createTicket(Long userId, CreateTicketRequestBean createTicketReq) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in createTicket() :: createTicketReq {}", createTicketReq.toString());
		User creator = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

		Ticket t = new Ticket();
		t.setTitle(createTicketReq.title.trim());
		t.setDescription(createTicketReq.description.trim());
		t.setStatus(TicketStatus.NEW);
		t.setCreatedBy(creator);

		t.setCurrentTextVersion(1);
		t.setDuplicateState("NONE");

		OffsetDateTime now = OffsetDateTime.now();
		t.setCreatedAt(now);
		t.setUpdatedAt(now);

		Ticket savedResp = ticketRepo.save(t);
		
		// using fetch join
        Ticket fetchTicket = ticketRepo.findByIdWithUsers(savedResp.getTicketId())
                .orElseThrow(() -> new NotFoundException("Ticket not found after create"));

        TicketResponseBean resp = setTicketResponseBean(fetchTicket);

		TICKET_SERVICE_LOG.info(
				"TicketServiceImpl :: exit createTicket() :: resp ticketId={} status={} , createdByUserId={}",
				resp.ticketId, resp.status, resp.createdByUserId);
		return resp;
	}
	
	@Transactional(readOnly = true)
	public TicketResponseBean getTicketById(Long ticketId) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in getTicketById() :: ticketId {}", ticketId);
		Ticket t = ticketRepo.findByIdWithUsers(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
		TicketResponseBean resp = setTicketResponseBean(t);
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit getTicketById() :: ticketId={}, status={}, createdBy={}, assignedTo={}",
				resp.ticketId, resp.status, resp.createdByUserId, resp.assignedToUserId);
		return resp;
	}

	@Transactional(readOnly = true)
	public List<TicketResponseBean> listTicketsForUser(Long userId) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in listTicketsForUser() :: userId {}", userId);
		userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
		List<TicketResponseBean> list = ticketRepo.findByCreatorWithUsers(userId)
                .stream()
                .map(this::setTicketResponseBean)
                .toList();

        TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit listTicketsForUser() :: userId={}, count={}", userId, list.size());
        return list;
	}
	
	@Transactional(readOnly = true)
    public List<TicketResponseBean> listAllTicketsForAdmin() {
        TICKET_SERVICE_LOG.info("TicketServiceImpl :: in listAllTicketsForAdmin()");

        List<TicketResponseBean> list = ticketRepo.findAllWithUsers()
                .stream()
                .map(this::setTicketResponseBean)
                .toList();

        TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit listAllTicketsForAdmin :: count={}", list.size());
        return list;
    }
	
	@Transactional(readOnly = true)
	public List<TicketResponseBean> listAssignedTicketsForAgent(Long agentUserId) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in listAssignedTicketsForAgent() :: agentUserId {}", agentUserId);
		userRepo.findById(agentUserId).orElseThrow(() -> new NotFoundException("User not found"));
		List<TicketResponseBean> list = ticketRepo.findTicketsAssignedToAgent(agentUserId)
                .stream()
                .map(this::setTicketResponseBean)
                .toList();

        TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit listTicketsForUser() :: agentUserId={}, count={}", agentUserId, list.size());
        return list;
	}

	private TicketResponseBean setTicketResponseBean(Ticket t) {
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: in setTicketResponseBean()");
		TicketResponseBean r = new TicketResponseBean();
		r.ticketId = t.getTicketId();
		r.title = t.getTitle();
		r.description = t.getDescription();
		r.status = t.getStatus();
		r.createdAt = t.getCreatedAt();
		r.updatedAt = t.getUpdatedAt();

		User creator = t.getCreatedBy();
        if (creator != null) {
            r.createdByUserId = creator.getUserId();
            r.createdByName = creator.getUsername();
            r.createdByEmail = creator.getEmail();
        }

        User assigned = t.getAssignedTo();
        if (assigned != null) {
            r.assignedToUserId = assigned.getUserId();
            r.assignedToName = assigned.getUsername();
            r.assignedToEmail = assigned.getEmail();
        }
        
		TICKET_SERVICE_LOG.info("TicketServiceImpl :: exit setTicketResponseBean() :: ticketId={}, status={}, assignedToUserId={}",
				r.ticketId, r.status, r.assignedToUserId);
		return r;
	}

	
}
