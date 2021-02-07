import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Action;
import model.ActionStatistic;
import model.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionAggregatorMemoryService implements ActionAggregatorService
{
   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   private final ObjectMapper objectMapper = new ObjectMapper();

   private final Map<String, List<Long>> actionTimes = new HashMap<>();
   private final Map<String, Statistics> actionStats = new HashMap<>();

   @Override
   public void addAction(String json)
   {
      try
      {
         Action action = objectMapper.readValue(json, Action.class);

         updateTimesAndStats(Collections.singletonList(action));
      }
      catch (JsonProcessingException e)
      {
         logger.error("Unable to deserialize json input. Actions will not be added", e);
      }
      catch (Exception e)
      {
         logger.error("Exception Processing json input. Actions will not be added", e);
      }
   }

   @Override
   public void addActions(String json)
   {
      try
      {
         List<Action> actions = objectMapper.readValue(json, new TypeReference<List<Action>>()
         {
         });

         updateTimesAndStats(actions);
      }
      catch (JsonProcessingException e)
      {
         logger.error("Unable to deserialize list. Actions will not be added", e);
      }
      catch (Exception e)
      {
         logger.error("Exception Processing json input. Actions will not be added", e);
      }
   }

   private synchronized void updateTimesAndStats(List<Action> actions)
   {
      //Group action list by action type
      actions.stream().collect(Collectors.groupingBy(Action::getAction))
            .forEach((key, actionsByType) -> {
               //either update the item entry or insert new entry
               if (actionTimes.containsKey(key))
               {
                  List<Long> times = Stream.concat(actionTimes.get(key).stream(), actionsByType.stream().map(Action::getTime)).collect(Collectors.toList());
                  actionTimes.put(key, times);
               }
               else
               {
                  actionTimes.put(key, actionsByType.stream().map(Action::getTime).collect(Collectors.toList()));
               }

               updateStats(key);
            });
   }

   private synchronized void updateStats(String actionKey)
   {
      List<Long> times = actionTimes.get(actionKey);

      if (times == null || times.isEmpty())
      {
         logger.info("will not calculate stats for empty action: {}", actionKey);
         return;
      }

      long total = 0;
      for (Long time : times)
      {
         total += time;
      }

      double average = (double) total / times.size();
      Statistics statistics = new Statistics();
      statistics.setAverage(average);

      actionStats.put(actionKey, statistics);
   }

   @Override
   public String getStats()
   {
      List<ActionStatistic> actionStatistics = actionStats.entrySet().stream().map(entrySet -> {
         ActionStatistic actionStatistic = new ActionStatistic();
         actionStatistic.setAction(entrySet.getKey());
         actionStatistic.setAvg(entrySet.getValue().getAverage());

         return actionStatistic;
      }).collect(Collectors.toList());

      try
      {
         return objectMapper.writeValueAsString(actionStatistics);
      }
      catch (JsonProcessingException e)
      {
         throw new RuntimeException("Unable to serialize list.", e);
      }
   }
}
