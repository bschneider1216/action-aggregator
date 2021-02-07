# Action Aggregator Project

## Assignment
The assignment is to write a small library class that can perform the following operations:

### Add Action
 ```addAction (string) returning error```
 
This function accepts a json serialized string of the form below and maintains an average time
for each action. 3 sample inputs:
```
   1. {"action":"jump", "time":100}
   2. {"action":"run", "time":75}
   3. {"action":"jump", "time":200}
```
Assume that an end user will be making concurrent calls into this function.

### Get Statistics
```getStats () returning string```

Write a second function that accepts no input and returns a serialized json array of the average
time for each action that has been provided to the addAction function. Output after the 3
sample calls above would be:
```javascript
[
   {"action":"jump", "avg":150},
   {"action":"run", "avg":75}
]
```

## Approach
I solved the core problem in Java8 in the `action-aggregator-core` module. The `ActionAggregatorMemoryService` implementation class allows adding actions as a list or single item. Jackson is used to deserialize the incoming json.
I enjoy the Spock Framework in Groovy for unit testing so built out a more comprehensive test  suite in the `ActionAggregatorMemoryServiceTest.groovy` class. A vanilla JUnit test was provided as well since Groovy was not one of the designated languages
Concurrent access is supported by java `synchronized` methods
I stored all occurences of each action and recalculate the average on each request. If working with very large datasets it would be more efficient to store just enough metadata to calculate the statistics request without iterating large lists.

A future iteration of this project may build out an application with a more distributed solution. This may include:
- a rest or shell interface for client applications
- a document datastore for storing action data and statistics
   - locking and consistency supported at the db-level
   - aggregations performed at the datastore level on-demand e.g. mongo aggregation pipelines, or streaming real time aggregations into a separate collection

### Tech Stack
- Java8 core library
  - Jackson JSON library
  - Lombok annotation processing
- Maven build management tool
- Groovy and the Spock testing framework for unit tests
- Application built using Spring Boot, Spring Shell/MVC, Kotlin, and MongoDB

## Run Instructions
- [install maven](http://maven.apache.org/install.html)
- [clone project](https://github.com/bschneider1216/action-aggregator.git)
- in the `action-aggregator` directory, compile the library and execute the unit tests via ```maven clean install```
