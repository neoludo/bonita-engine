package org.bonitasoft.engine.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.EventCriterion;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TimerEventTest extends CommonAPITest {

    private User user;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(user.getId());
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
         loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser(USERNAME, PASSWORD);
        logoutThenloginAs(USERNAME, PASSWORD);
    }

    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Timer event", "Intermediate catch event", "User task" }, story = "Execute process with an intermediate catch event with a timer duration type.", jira = "")
    @Test
    public void timerIntermediateCatchEventDuration() throws Exception {
        final String step1Name = "step1";
        final String step2Name = "step2";
        final Expression timerExpression = new ExpressionBuilder().createConstantLongExpression(1000); // the timer intermediate catch event will wait one
                                                                                                       // second
        final ProcessDefinition definition = deployProcessWithTimerIntermediateCatchEventAndUserTask(TimerType.DURATION, timerExpression, step1Name, step2Name);

        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final ActivityInstance userTask = waitForUserTask(step1Name, processInstance);
        assignAndExecuteStep(userTask, getIdentityAPI().getUserByUserName(USERNAME).getId());

        waitForEventInWaitingState(processInstance, "intermediateCatchEvent");
        final long processInstanceId = processInstance.getId();
        EventInstance eventInstance = getEventInstance(processInstanceId, "intermediateCatchEvent");
        checkIntermediateCatchEventInstance(eventInstance, "intermediateCatchEvent", TestStates.WAITING);
        // wait trigger activation
        int cnt = 0;

        // BS-9586 : for mysql, we wait longer
        while (cnt < 10 && eventInstance != null) {
            Thread.sleep(1000);
        eventInstance = getEventInstance(processInstanceId, "intermediateCatchEvent");
            cnt++;
        }
        assertNull(eventInstance);// finished

        waitForUserTask(step2Name, processInstance);

        disableAndDeleteProcess(definition);
    }

    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Timer event", "Intermediate catch event", "User task" }, story = "Execute process with an intermediate catch event with a timer date type.", jira = "")
    @Test
    public void timerIntermediateCatchEventDate() throws Exception {
        final String step1Name = "step1";
        final String step2Name = "step2";
        final long expectedDate = System.currentTimeMillis() + 5000;
        final Expression timerExpression = new ExpressionBuilder().createGroovyScriptExpression("testTimerIntermediateCatchEventDate", "return new Date("
                + expectedDate + "l)", Date.class.getName()); // the timer intermediate catch
        // event will wait one second
        final ProcessDefinition definition = deployProcessWithTimerIntermediateCatchEventAndUserTask(TimerType.DATE, timerExpression, step1Name, step2Name);

        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final ActivityInstance userTask = waitForUserTask(step1Name, processInstance);
        assertNotNull(userTask);

        assignAndExecuteStep(userTask, getIdentityAPI().getUserByUserName(USERNAME).getId());

        waitForFlowNodeInState(processInstance, "intermediateCatchEvent", TestStates.WAITING, true);
        final EventInstance eventInstance = getEventInstance(processInstance.getId(), "intermediateCatchEvent");
        checkIntermediateCatchEventInstance(eventInstance, "intermediateCatchEvent", TestStates.WAITING);
        // wait trigger activation
        waitForUserTask(step2Name, processInstance);
        final long now = System.currentTimeMillis();
        assertTrue("Event has triggered too early !" + (now - expectedDate), expectedDate <= now);

        disableAndDeleteProcess(definition);
    }

    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Timer event", "Start event", "User task" }, story = "Execute a process with a start event with a timer date type.", jira = "")
    @Test
    public void timerStartEventDate() throws Exception {
        final String stepName = "step1";
        final long expectedDate = System.currentTimeMillis() + 1000;
        final Expression timerExpression = new ExpressionBuilder().createGroovyScriptExpression("testTimerStartEventDate", "return new Date(" + expectedDate
                + "l);", Date.class.getName()); // the new instance must be
        // created in one second
        final ProcessDefinition definition = deployProcessWithTimerStartEventAndUserTask(TimerType.DATE, timerExpression, stepName);

        final List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.CREATION_DATE_DESC);
        assertTrue(processInstances.isEmpty());

        // wait for process instance creation
        waitForUserTask(stepName);

        disableAndDeleteProcess(definition);
    }

    //    uncomment to repeat random test
    //    @Rule
    //    public RepeatRule repeatRule = new RepeatRule();
    //
    //    @Repeat(times = 100)
    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Timer event", "Start event", "User task" }, story = "Execute a process with a start event with a timer cycle type.", jira = "")
    @Test
    public void timerStartEventCycle() throws Exception {
        final Expression timerExpression = new ExpressionBuilder().createConstantStringExpression("*/1 * * * * ?"); // new instance created every second
        final String stepName = "step1";
        final ProcessDefinition definition = deployProcessWithTimerStartEventAndUserTask(TimerType.CYCLE, timerExpression, stepName);

        //when
        Thread.sleep(2000);

        //then
        final List<ProcessInstance> firstProcessInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.CREATION_DATE_DESC);
        assertThat(firstProcessInstances).as("should have started first instance").isNotEmpty();

        //when
        Thread.sleep(2000);

        //then
        final List<ProcessInstance> secondProcessInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.CREATION_DATE_DESC);
        assertThat(secondProcessInstances).as("should have started another instance").isNotEmpty();
        assertThat(secondProcessInstances.size()).as("should have started another instance").isGreaterThan(firstProcessInstances.size());

        //cleanup
        waitForUserTask(stepName, secondProcessInstances.get(secondProcessInstances.size() - 1));
        disableAndDeleteProcess(definition);
    }

    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Timer event", "Start event", "User task" }, story = "Execute a process with a start event with a timer duration type.", jira = "")
    @Test
    public void timerStartEventDuration() throws Exception {
        final String stepName = "step1";
        final Expression timerExpression = new ExpressionBuilder().createConstantLongExpression(1500); // the new instance must be created in one second
        final ProcessDefinition definition = deployProcessWithTimerStartEventAndUserTask(TimerType.DURATION, timerExpression, stepName);

        List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.CREATION_DATE_DESC);
        assertTrue(processInstances.isEmpty());

        // wait for process instance creation
        Thread.sleep(4000);

        processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.CREATION_DATE_DESC);
        assertEquals(1, processInstances.size());

        waitForUserTask(stepName, processInstances.get(0));
        disableAndDeleteProcess(definition);
    }

    private EventInstance getEventInstance(final long processInstanceId, final String eventName) throws RetrieveException {
        final List<EventInstance> eventInstances = getProcessAPI().getEventInstances(processInstanceId, 0, 10, EventCriterion.NAME_ASC);
        EventInstance searchedEventInstance = null;
        final Iterator<EventInstance> iterator = eventInstances.iterator();
        while (iterator.hasNext() && searchedEventInstance == null) {
            final EventInstance eventInstance = iterator.next();
            if (eventInstance.getName().equals(eventName)) {
                searchedEventInstance = eventInstance;
            }
        }
        return searchedEventInstance;
    }

    private void checkIntermediateCatchEventInstance(final EventInstance eventInstance, final String eventName, final TestStates state) {
        assertTrue(eventInstance instanceof IntermediateCatchEventInstance);
        checkEventInstance(eventInstance, eventName, state);
    }

    private void checkEventInstance(final EventInstance eventInstance, final String eventName, final TestStates state) {
        assertEquals(eventName, eventInstance.getName());
        assertEquals(state.getStateName(), eventInstance.getState());
        // if(TestStates.getNormalFinalState(eventInstance).equals(state)) {
        // assertTrue(eventInstance.getEndDate() > 0);
        // }
    }

    private ProcessDefinition deployProcessWithTimerIntermediateCatchEventAndUserTask(final TimerType timerType, final Expression timerValue,
            final String step1Name, final String step2Name) throws BonitaException {
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("My Process with start event", "1.0")
                .addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addStartEvent("startEvent").addUserTask(step1Name, ACTOR_NAME)
                .addIntermediateCatchEvent("intermediateCatchEvent").addTimerEventTriggerDefinition(timerType, timerValue).addUserTask(step2Name, ACTOR_NAME)
                .addEndEvent("endEvent").addTransition("startEvent", step1Name).addTransition(step1Name, "intermediateCatchEvent")
                .addTransition("intermediateCatchEvent", step2Name).addTransition(step2Name, "endEvent").getProcess();
        final ProcessDefinition definition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        return definition;
    }

    private ProcessDefinition deployProcessWithTimerStartEventAndUserTask(final TimerType timerType, final Expression timerValue, final String stepName)
            throws BonitaException {
        final String delivery = "Delivery men";
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("My Process with start event", "1.0")
                .addActor(delivery).addDescription("Delivery all day and night long").addStartEvent("startEvent")
                .addTimerEventTriggerDefinition(timerType, timerValue).addUserTask(stepName, delivery).addEndEvent("endEvent")
                .addTransition("startEvent", stepName).addTransition(stepName, "endEvent").getProcess();

        final ProcessDefinition definition = deployAndEnableProcessWithActor(designProcessDefinition, delivery, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        return definition;
    }

}
