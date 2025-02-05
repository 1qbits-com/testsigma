package com.testsigma.step.processors;

import com.testsigma.dto.TestCaseEntityDTO;
import com.testsigma.dto.TestCaseStepEntityDTO;
import com.testsigma.dto.TestStepDTO;
import com.testsigma.exception.TestsigmaException;
import com.testsigma.model.WorkspaceType;
import com.testsigma.model.Element;
import com.testsigma.model.TestDataSet;
import com.testsigma.model.TestStepType;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class WhileLoopStepProcessor extends StepProcessor {
  public WhileLoopStepProcessor(WebApplicationContext webApplicationContext, List<TestCaseStepEntityDTO> testCaseStepEntityDTOS,
                                WorkspaceType workspaceType, Map<String, Element> elementMap,
                                TestStepDTO testStepDTO, Long executionId, TestDataSet testDataSet,
                                Map<String, String> environmentParams, TestCaseEntityDTO testCaseEntityDTO,
                                String environmentParamSetName, String dataProfile, List<String> testDataPasswords,
                                List<String> environmentPasswords) {
    super(webApplicationContext, testCaseStepEntityDTOS, workspaceType, elementMap, testStepDTO, executionId, testDataSet,
      environmentParams, testCaseEntityDTO, environmentParamSetName, dataProfile, testDataPasswords, environmentPasswords);
  }

  public void processWhileLoop(List<TestStepDTO> testStepDTOS, List<Long> loopIds)
    throws TestsigmaException, CloneNotSupportedException {

    loadLoop(testStepDTO, testStepDTOS, loopIds);
    List<TestCaseStepEntityDTO> entityList = new ArrayList<>();

    if (testStepDTO.getTestStepDTOS() != null && testStepDTO.getTestStepDTOS().size() > 0) {
      TestStepDTO entity = testStepDTO.clone();
      TestCaseStepEntityDTO iteEntity = new TestCaseStepEntityDTO();
      iteEntity.setId(entity.getId());
      TestStepDTO loopentity = entity.getTestStepDTOS().get(0);
      TestCaseStepEntityDTO exeEntity = null;
      if (loopentity.getType() == TestStepType.REST_STEP) {
        new RestStepProcessor(webApplicationContext, iteEntity.getTestCaseSteps(), workspaceType, elementMap,
          loopentity, executionId, testDataSet, environmentParameters, testCaseEntityDTO, environmentParamSetName,
          dataProfile, testDataPasswords, environmentPasswords).process();
        exeEntity = iteEntity.getTestCaseSteps().get(iteEntity.getTestCaseSteps().size() - 1);
        iteEntity.getTestCaseSteps().remove(iteEntity.getTestCaseSteps().size() - 1);
      } else {
        exeEntity = new StepProcessor(webApplicationContext, testCaseStepEntityDTOS, workspaceType, elementMap,
          loopentity, executionId, testDataSet, environmentParameters, testCaseEntityDTO, environmentParamSetName,
          dataProfile, testDataPasswords, environmentPasswords).processStep();

        exeEntity.setParentId(loopentity.getParentId());
        exeEntity.setTestCaseId(loopentity.getTestCaseId());
        exeEntity.setConditionType(loopentity.getConditionType());
        exeEntity.setPriority(loopentity.getPriority());
        exeEntity.setPreRequisite(loopentity.getPreRequisiteStepId());
        exeEntity.setType(loopentity.getType());
        exeEntity.setStepGroupId(loopentity.getStepGroupId());
        exeEntity.setPosition(loopentity.getPosition());
      }
      for (TestStepDTO centity : loopentity.getTestStepDTOS()) {
        List<TestCaseStepEntityDTO> stepGroupSpecialSteps = new ArrayList<>();
        if (centity.getType() == TestStepType.REST_STEP) {
          new RestStepProcessor(webApplicationContext, stepGroupSpecialSteps, workspaceType, elementMap,
            centity, executionId, testDataSet, environmentParameters, testCaseEntityDTO, environmentParamSetName, dataProfile,
            testDataPasswords, environmentPasswords).process();
          exeEntity.getTestCaseSteps().addAll(stepGroupSpecialSteps);
          continue;
        }

        if (TestStepType.FOR_LOOP == centity.getType()) {
          new ForLoopStepProcessor(webApplicationContext, stepGroupSpecialSteps, workspaceType,
            elementMap, centity, executionId, testDataSet, environmentParameters, testCaseEntityDTO,
            environmentParamSetName, dataProfile, testDataPasswords, environmentPasswords)
            .processLoop(loopentity.getTestStepDTOS(), loopIds);
          exeEntity.getTestCaseSteps().addAll(stepGroupSpecialSteps);

          continue;
        }

        TestCaseStepEntityDTO cstepEntity = new StepProcessor(webApplicationContext, testCaseStepEntityDTOS,
                workspaceType, elementMap, centity, executionId, testDataSet, environmentParameters, testCaseEntityDTO,
          environmentParamSetName, dataProfile, testDataPasswords, environmentPasswords).processStep();

        cstepEntity.setParentId(centity.getParentId());
        cstepEntity.setTestCaseId(centity.getTestCaseId());
        cstepEntity.setConditionType(centity.getConditionType());
        cstepEntity.setPriority(centity.getPriority());
        cstepEntity.setPreRequisite(centity.getPreRequisiteStepId());
        cstepEntity.setType(centity.getType());
        cstepEntity.setStepGroupId(centity.getStepGroupId());

        for (TestStepDTO stepDTOFromGroup : centity.getTestStepDTOS()) {
          if (stepDTOFromGroup.getType() == TestStepType.REST_STEP) {
            new RestStepProcessor(webApplicationContext, cstepEntity.getTestCaseSteps(), workspaceType,
              elementMap, stepDTOFromGroup, executionId, testDataSet, environmentParameters, testCaseEntityDTO,
              environmentParamSetName, dataProfile, testDataPasswords, environmentPasswords).process();
            continue;
          }
          TestCaseStepEntityDTO stepEntityFromGroup = new StepProcessor(webApplicationContext, testCaseStepEntityDTOS,
                  workspaceType, elementMap, stepDTOFromGroup, executionId, testDataSet, environmentParameters,
            testCaseEntityDTO, environmentParamSetName, dataProfile, testDataPasswords, environmentPasswords)
            .processStep();

          if (TestStepType.FOR_LOOP == stepDTOFromGroup.getType()) {
            new ForLoopStepProcessor(webApplicationContext, stepEntityFromGroup.getTestCaseSteps(), workspaceType,
              elementMap, stepDTOFromGroup, executionId, testDataSet, environmentParameters, testCaseEntityDTO,
              environmentParamSetName, dataProfile, testDataPasswords, environmentPasswords)
              .processLoop(null, loopIds);
            continue;
          }

          stepEntityFromGroup.setParentId(stepDTOFromGroup.getParentId());
          stepEntityFromGroup.setTestCaseId(stepDTOFromGroup.getTestCaseId());
          stepEntityFromGroup.setConditionType(stepDTOFromGroup.getConditionType());
          stepEntityFromGroup.setPriority(stepDTOFromGroup.getPriority());
          stepEntityFromGroup.setPreRequisite(stepDTOFromGroup.getPreRequisiteStepId());
          stepEntityFromGroup.setType(stepDTOFromGroup.getType());
          stepEntityFromGroup.setStepGroupId(stepDTOFromGroup.getStepGroupId());
          cstepEntity.getTestCaseSteps().add(stepEntityFromGroup);
        }
        exeEntity.getTestCaseSteps().add(cstepEntity);
      }

      iteEntity.getTestCaseSteps().add(exeEntity);
      iteEntity.setParentId(entity.getParentId());
      iteEntity.setTestCaseId(entity.getTestCaseId());
      iteEntity.setConditionType(entity.getConditionType());
      iteEntity.setPriority(entity.getPriority());
      iteEntity.setPreRequisite(entity.getPreRequisiteStepId());
      iteEntity.setPosition(entity.getId().intValue());
      iteEntity.setWaitTime(entity.getWaitTime() == null ? 0 : entity.getWaitTime());
      iteEntity.setIndex(1);
      iteEntity.setType(entity.getType());
      iteEntity.setNaturalTextActionId(entity.getNaturalTextActionId());
      populateStepDetails(testStepDTO, iteEntity);
      iteEntity.setAction(entity.getAction());
      entityList.add(iteEntity);
    }
    testCaseStepEntityDTOS.addAll(entityList);
  }
}
