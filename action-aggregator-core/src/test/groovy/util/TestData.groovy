package util

import com.fasterxml.jackson.databind.ObjectMapper
import model.Action

class TestData
{
   static ObjectMapper objectMapper = new ObjectMapper()

   static def json1 = objectMapper.writeValueAsString(new Action(action: 'jump', time: 100))

   static def jsonList1 = objectMapper.writeValueAsString([new Action(action: 'jump', time: 100)])

   static def jsonList2 = objectMapper.writeValueAsString([
         new Action(action: 'jump', time: 50),
         new Action(action: 'jump', time: 25),
         new Action(action: 'jump', time: 225),
         new Action(action: 'jump', time: 100)

   ])

   static def list3 = [
         new Action(action: 'jump', time: 100),
         new Action(action: 'run', time: 50),
         new Action(action: 'dance', time: 25),
         new Action(action: 'jump', time: 150),
         new Action(action: 'run', time: 50),
         new Action(action: 'run', time: 110),
         new Action(action: 'dance', time: 75),
         new Action(action: 'eat', time: 10000)
   ]

   static def jsonList3 = objectMapper.writeValueAsString(list3)

}
