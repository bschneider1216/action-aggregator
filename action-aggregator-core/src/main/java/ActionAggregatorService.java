public interface ActionAggregatorService
{
   void addAction(String json);

   void addActions(String json);

   String getStats();
}
