import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ActionAggregatorServiceTest
{
   static String json1;

   @SneakyThrows
   @BeforeAll
   static void init()
   {
      Class clazz = ActionAggregatorServiceTest.class;
      InputStream inputStream = clazz.getResourceAsStream("/json1.json");
      json1 = readFromInputStream(inputStream);

      inputStream.close();
   }

   @Test
   void addActions_happyPath()
   {
      ActionAggregatorMemoryService actionAggregatorService = new ActionAggregatorMemoryService();

      actionAggregatorService.addAction(json1);
      String stats = actionAggregatorService.getStats();

      assertNotNull(stats);
   }

   private static String readFromInputStream(InputStream inputStream)
         throws IOException
   {
      StringBuilder resultStringBuilder = new StringBuilder();
      try (BufferedReader br
                 = new BufferedReader(new InputStreamReader(inputStream)))
      {
         String line;
         while ((line = br.readLine()) != null)
         {
            resultStringBuilder.append(line).append("\n");
         }
      }
      return resultStringBuilder.toString();
   }

}


