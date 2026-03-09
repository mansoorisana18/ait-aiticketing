package com.aiticketing.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiticketing.ai.dto.RoutingResult;
import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.User;
import com.aiticketing.entity.enums.TicketStatus;
import com.aiticketing.entity.enums.UserRole;
import com.aiticketing.repository.TicketRepository;
import com.aiticketing.repository.UserRepository;

@Service
public class RoutingService {

	private static final Logger ROUTING_LOG = LoggerFactory.getLogger(RoutingService.class);
	
    private final UserRepository userRepo;
    private final TicketRepository ticketRepo;

    public RoutingService(UserRepository userRepo, TicketRepository ticketRepo) {
        this.userRepo = userRepo;
        this.ticketRepo = ticketRepo;
    }
    
    //Our ticket categories == departments having agents
    //assign ticket if it is currently unassigned and returns the least loaded agent for the given department category
    //We save the ticket in memory & persist it in further steps
    @Transactional(readOnly = true)
    public RoutingResult assignIfPossible(Ticket t, String departmentCategory) {
    	ROUTING_LOG.info("RoutingService :: in assignIfPossible() :: dept={}", departmentCategory);
    	
    	RoutingResult result = new RoutingResult();
    	result.department = departmentCategory;
    	
    	if (t.getAssignedTo() != null) {
        	ROUTING_LOG.info("RoutingService :: in assignIfPossible() :: already assigned :: ticketId={} assignedTo={}",
                    t.getTicketId(), t.getAssignedTo().getUserId());
        	
        	result.outcome = RoutingResult.Outcome.ASSIGNED;
            result.selectedAgentId = t.getAssignedTo().getUserId();
            result.selectedWorkload = null;
            result.eligibleAgentCount = 1;
            return result;
        }
    	
    	List<User> agents = userRepo.findByRoleAndDepartment(UserRole.AGENT, departmentCategory);
    	result.eligibleAgentCount = agents.size();
    	
    	//is redundant: can be removed
        if (agents.isEmpty()) {
        	ROUTING_LOG.warn("RoutingService :: in assignIfPossible() :: no Agents found for the department :: dept={}", departmentCategory);
            result.outcome = RoutingResult.Outcome.NO_ELIGIBLE_AGENT;
        	return result; //no agent available; leave ticket unassigned
        }

        //Make list of all agent ids retrieved
        List<Long> agentIds = agents.stream().map(User::getUserId).toList();
        
        //Tickets with READY & IN_PROGRESS status are considered as active workload
        List<TicketStatus> activeStatuses = List.of(TicketStatus.READY, TicketStatus.IN_PROGRESS);

        List<Object[]> groupedCounts = ticketRepo.countActiveByAgentIds(agentIds, activeStatuses);

        Map<Long, Long> workloadMap = new HashMap<>();
        for (Object[] row : groupedCounts) {
            Long agentId = (Long) row[0];
            Long count = (Long) row[1];
            workloadMap.put(agentId, count);
        }

        User bestAgent = null;
        long bestLoad = Long.MAX_VALUE;

        for (User agent : agents) {
            long load = workloadMap.getOrDefault(agent.getUserId(), 0L);

            ROUTING_LOG.debug("RoutingService :: in assignIfPossible() :: candidate agentId={} workload={}",
                    agent.getUserId(), load);

            if (load < bestLoad) {
                bestLoad = load;
                bestAgent = agent;
            }
        }

        if (bestAgent == null) {
        	ROUTING_LOG.warn("RoutingService :: exit assignIfPossible() :: no best agent found department={}", departmentCategory);
        	result.outcome = RoutingResult.Outcome.NO_ELIGIBLE_AGENT;
        	return result;
                    
        } 
        
        t.setAssignedTo(bestAgent);
        result.outcome = RoutingResult.Outcome.ASSIGNED;
        result.selectedAgentId = bestAgent.getUserId();
        result.selectedWorkload = bestLoad;
        
        ROUTING_LOG.info("RoutingService :: exit assignIfPossible() :: ticketId={} assigned to agentId={} workload={}", 
        		t.getTicketId(), bestAgent.getUserId(), bestLoad);

        return result;
    }
}