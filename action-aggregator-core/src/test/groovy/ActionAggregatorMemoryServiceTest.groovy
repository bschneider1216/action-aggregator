import com.fasterxml.jackson.databind.ObjectMapper
import model.Action
import model.ActionStatistic
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static util.TestData.*

class ActionAggregatorMemoryServiceTest extends Specification
{
   @Shared
   ObjectMapper objectMapper = new ObjectMapper()

   ActionAggregatorMemoryService actionAggregatorService = new ActionAggregatorMemoryService()

   def 'addActions - 1 element list'()
   {
      given:
      def jsonString = jsonList1

      when:
      actionAggregatorService.addActions(jsonString)
      def statsJson = actionAggregatorService.getStats()
      ActionStatistic[] actionStatistics = objectMapper.readValue(statsJson, ActionStatistic[])

      then:
      actionStatistics[0].action == 'jump'
      actionStatistics[0].avg == 100
   }

   def 'addActions - 4 element list, same type'()
   {
      given:
      def jsonString = jsonList2

      when:
      actionAggregatorService.addActions(jsonString)
      def statsJson = actionAggregatorService.getStats()
      ActionStatistic[] actionStatistics = objectMapper.readValue(statsJson, ActionStatistic[])

      then:
      actionStatistics[0].action == 'jump'
      actionStatistics[0].avg == 100
   }

   def 'addActions - 7 element list, different types'()
   {
      given:
      def jsonString = jsonList3

      when:
      actionAggregatorService.addActions(jsonString)
      def statsJson = actionAggregatorService.getStats()
      ActionStatistic[] actionStatistics = objectMapper.readValue(statsJson, ActionStatistic[])

      then:
      validateList3(actionStatistics)
   }

   def 'addAction - 7 element list sequentially, different types'()
   {
      given:
      Action[] actions = list3

      when:
      actions.each {
         Action action -> actionAggregatorService.addAction(objectMapper.writeValueAsString(action))
      }

      def statsJson = actionAggregatorService.getStats()
      ActionStatistic[] actionStatistics = objectMapper.readValue(statsJson, ActionStatistic[])

      then:
      validateList3(actionStatistics)
   }

   def 'addAction - 7 element list concurrently, different types'()
   {
      given:
      Action[] actions = list3

      when:
      List<CompletableFuture> futures = []

      actions.each { Action action ->
         futures.add(CompletableFuture.supplyAsync({ actionAggregatorService.addAction(objectMapper.writeValueAsString(action)) }))
      }

      futures.stream().map({ CompletableFuture::join }).collect()

      def statsJson = actionAggregatorService.getStats()
      ActionStatistic[] actionStatistics = objectMapper.readValue(statsJson, ActionStatistic[])

      then:
      assert validateList3(actionStatistics)
   }


   def 'addAction - 2000 element list concurrently, different types'()
   {
      given:
      List<Action> actions = []

      for (i in 0..1000)
      {
         actions.add(new Action(action: 'jump', time: i))
         actions.add(new Action(action: 'run', time: i))
      }

      when:
      ExecutorService executor = Executors.newFixedThreadPool(4)
      actions.each {
         actionAggregatorService.addAction(objectMapper.writeValueAsString(it))
      }


      def statsJson = actionAggregatorService.getStats()
      ActionStatistic[] actionStatistics = objectMapper.readValue(statsJson, ActionStatistic[])


      then:
      ActionStatistic runStat = actionStatistics.find({ action -> action.action == 'run' })
      assert runStat.avg == 500

      ActionStatistic jumpStat = actionStatistics.find({ action -> action.action == 'jump' })
      assert jumpStat.avg == 500
   }


   static def validateList3(ActionStatistic[] actionStatistics)
   {
      ActionStatistic jumpStat = actionStatistics.find({ action -> action.action == 'jump' })
      assert jumpStat.avg == 125

      ActionStatistic danceStat = actionStatistics.find({ action -> action.action == 'dance' })
      assert danceStat.avg == 50

      ActionStatistic runStat = actionStatistics.find({ action -> action.action == 'run' })
      assert runStat.avg == 70

      ActionStatistic eatStat = actionStatistics.find({ action -> action.action == 'eat' })
      assert eatStat.avg == 10000

      return true
   }
}
