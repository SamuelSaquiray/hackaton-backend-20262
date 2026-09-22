package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.entity.*;
import com.tuckersoft.branchengine.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DecisionServiceTest {
    @Mock DecisionRepository decisions;
    @Mock PlaythroughRepository plays;
    @Mock StoryNodeRepository nodes;
    @Mock ApplicationEventPublisher publisher;
    DecisionService service;
    @BeforeEach void setup(){MockitoAnnotations.openMocks(this);service=new DecisionService(decisions,plays,nodes,publisher);}
    @Test void precedenciaRebeldiaAntesDeRuptura(){assertEquals("REBELDIA",service.classify(service.normalize("Stefan destruye la camara")));}
    @Test void entradaCorruptaNoTieneLetras(){assertEquals("ENTRADA_CORRUPTA",service.classify(service.normalize("123456 !!!")));}

    @Test void criticoResta45YSuma40(){
        Playthrough p=play("TAG");p.setLucidity(100);p.setControlLevel(0);StoryNode n=node("A");p.setCurrentNode(n);
        when(plays.findById(1L)).thenReturn(java.util.Optional.of(p)); when(decisions.save(any())).thenAnswer(i->i.getArgument(0)); when(plays.save(any())).thenAnswer(i->i.getArgument(0));
        when(nodes.findByNodeCode("B")).thenReturn(java.util.Optional.of(node("B")));
        var r=service.decide(new com.tuckersoft.branchengine.dto.DecisionDtos.CreateRequest(1L,"una decision normal","CRITICO"),user(),false,false);
        assertEquals(55,r.lucidity()); assertEquals(40,r.controlLevel());
    }
    @Test void lucidityTienePrioridadAlFinal(){Playthrough p=play("TAG");p.setLucidity(45);p.setControlLevel(60);StoryNode n=node("A");n.setGlitchBranchCode("B");p.setCurrentNode(n);
        when(plays.findById(1L)).thenReturn(java.util.Optional.of(p));when(decisions.save(any())).thenAnswer(i->i.getArgument(0));when(plays.save(any())).thenAnswer(i->i.getArgument(0));
        var r=service.decide(new com.tuckersoft.branchengine.dto.DecisionDtos.CreateRequest(1L,"una decision normal","CRITICO"),user(),false,false);
        assertEquals("ENDING_WHITE_BEAR",r.endingCode());
    }
    @Test void publicaEventoSoloNormal(){
        Playthrough p=play("TAG");StoryNode n=node("A");p.setCurrentNode(n);when(plays.findById(1L)).thenReturn(java.util.Optional.of(p));when(decisions.save(any())).thenAnswer(i->i.getArgument(0));when(plays.save(any())).thenAnswer(i->i.getArgument(0));
        service.decide(new com.tuckersoft.branchengine.dto.DecisionDtos.CreateRequest(1L,"una decision normal","LEVE"),user(),false,false);
        verify(publisher,times(1)).publishEvent(any());
    }
    private User user(){User u=new User();u.setId(1L);u.setEmail("a@a.com");u.setRole("ROLE_USER");return u;}
    private Playthrough play(String tag){Playthrough p=new Playthrough();p.setId(1L);p.setPlayerTag(tag);p.setUser(user());p.setStatus("ACTIVA");p.setLucidity(100);p.setControlLevel(0);return p;}
    private StoryNode node(String code){StoryNode n=new StoryNode();n.setNodeCode(code);n.setPrimaryBranchCode("B");return n;}
}
