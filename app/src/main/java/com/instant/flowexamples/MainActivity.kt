package com.instant.flowexamples

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {

    private val TAG = "OutputFlow"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch each flow example in its own coroutine for independent execution.
        lifecycleScope.launch { coldFlowExample() }
        lifecycleScope.launch { hotFlowExample() }
        lifecycleScope.launch { flowWithOperatorsExample() }
        lifecycleScope.launch { flowBuilderExamples() }
        lifecycleScope.launch { flowWithContextExample() }
        lifecycleScope.launch { flowErrorHandlingExample() }
        lifecycleScope.launch { flowCancellationExample() }
        lifecycleScope.launch { stateFlowExample() }
        lifecycleScope.launch { sharedFlowExample() }
        lifecycleScope.launch { backpressureHandlingExample() }
        lifecycleScope.launch { combineMultipleFlowsExample() }
        lifecycleScope.launch { flatMapConcatExample() }  // New example
        lifecycleScope.launch { conflateExample() }       // New example
        lifecycleScope.launch { terminalOperatorsExample() } // New example
    }

    // 1. Cold Flow Example
    // Cold flows are lazy and only emit values when collected. Each collector gets the same sequence.
    private suspend fun coldFlowExample() {
        val coldFlow = flow {
            emit(1)  // Emit value 1
            emit(2)  // Emit value 2
            emit(3)  // Emit value 3
        }
        // Collecting the flow will trigger it to emit values
        coldFlow.collect { value ->
            Log.d(TAG, "Cold Flow collected: $value")
        }
    }

    // 2. Hot Flow Example (StateFlow)
    // Hot flows (like StateFlow) emit values continuously, regardless of whether collectors are present.
    private suspend fun hotFlowExample() {
        val stateFlow = MutableStateFlow(0) // Initial value 0
        stateFlow.value = 1                 // Update to value 1
        stateFlow.value = 2                 // Update to value 2

        // Collect the flow to receive the latest value
        stateFlow.collect { value ->
            Log.d(TAG, "Hot Flow (StateFlow) collected: $value")
        }
    }

    // 3. Flow with Operators (map, filter, take)
    // Use flow operators to transform, filter, and control emissions from a flow.
    private suspend fun flowWithOperatorsExample() {
        val flow = flowOf(1, 2, 3, 4, 5) // Create a flow with fixed values

        // Apply operators: map to double values, filter even numbers, and take only 3 results
        flow
            .map { it * 2 }              // Multiply each emitted value by 2
            .filter { it % 2 == 0 }      // Only keep even values
            //.take(3)                     // Take the first 3 emissions
            .collect { value ->
                Log.d(TAG, "Flow with operators collected: $value")
            }
    }

    // 4. Flow Builders (flow, flowOf, asFlow)
    // Create flows using different builders: flow, flowOf, and asFlow.
    private suspend fun flowBuilderExamples() {
        // flow: Used for emitting values over time or on-demand
        val flowExample = flow {
            for (i in 1..3) {
                delay(100) // Simulate a delay before each emission
                emit(i)     // Emit the value
            }
        }

        // flowOf creates a flow with predefined values
        val flowOfExample = flowOf(1, 2, 3)

        // asFlow converts a collection into a flow
        val listFlow = listOf(1, 2, 3).asFlow()

        // Collect and print values from the flows
        flowExample.collect { Log.d(TAG, "flow collected: $it") }
        flowOfExample.collect { Log.d(TAG, "flowOf collected: $it") }
        listFlow.collect { Log.d(TAG, "asFlow collected: $it") }
    }

    // 5. Flow with Context Switching (flowOn)
    // Switch context for upstream flow operations using flowOn. Useful for background tasks.
    private suspend fun flowWithContextExample() {
        val flow = flow {
            emit("Flow data")
        }.flowOn(Dispatchers.IO) // Switch to IO thread for emitting values

        // Collect the flow on the main thread
        flow.collect { value ->
            Log.d(TAG, "Collected from flowOn: $value")
        }
    }

    // 6. Flow with Error Handling (catch)
    // Handle exceptions in flow emissions using the catch operator.
    private suspend fun flowErrorHandlingExample() {
        val numbersFlow = flow {
            emit(1)
            throw Exception("Error in flow") // Simulate an exception
        }.catch { exception ->
            Log.e(TAG, "Caught exception: $exception")
        }

        // Collect the flow (the exception will be caught and handled)
        numbersFlow.collect { value ->
            Log.d(TAG, "Flow with error handling collected: $value")
        }
    }

    // 7. Flow Cancellation Example
    // Demonstrates flow cancellation with timeout. Collect the flow until the timeout is reached.
    private suspend fun flowCancellationExample() {
        val longRunningFlow = flow {
            for (i in 1..5) {
                delay(1000)  // Simulate a long-running task
                emit(i)      // Emit values with a delay
            }
        }

        // Cancel the flow collection after 3 seconds
        withTimeoutOrNull(3000) { // Cancel after 3 seconds
            longRunningFlow.collect { value ->
                Log.d(TAG, "Collected with cancellation: $value")
            }
        }
    }

    // 8. StateFlow Example
    // StateFlow is used to hold and emit the latest state, emitting it to collectors.
    private suspend fun stateFlowExample() {
        val stateFlow = MutableStateFlow(0) // Initial state

        // Change the state
        stateFlow.value = 1
        stateFlow.value = 2

        // Collect the latest state
        stateFlow.collect { value ->
            Log.d(TAG, "StateFlow collected: $value")
        }
    }

    // 9. SharedFlow Example
    // SharedFlow is a hot flow that broadcasts events to multiple collectors.
    private suspend fun sharedFlowExample() {
        val sharedFlow = MutableSharedFlow<String>(replay = 1) // Replay the last event to new collectors

        // Emit events into the shared flow
        sharedFlow.emit("Event 1")
        sharedFlow.emit("Event 2")

        // Collect the events
        sharedFlow.collect { event ->
            Log.d(TAG, "SharedFlow collected: $event")
        }
    }

    // 10. Handling Backpressure with buffer and conflate
    // Use buffer or conflate to handle cases where the producer is faster than the consumer.
    private suspend fun backpressureHandlingExample() {
        val fastProducerFlow = flow {
            for (i in 1..10) {
                emit(i)       // Emit values quickly
                delay(100)    // Fast emission rate
            }
        }

        // Use buffer to prevent blocking the fast producer
        fastProducerFlow
            .buffer()       // Buffer emissions for smoother collection
            .collect { value ->
                delay(500)   // Simulate slower consumer
                Log.d(TAG, "Collected with buffering: $value")
            }
    }

    // 11. Combining Multiple Flows (combine, zip)
    // Combine multiple flows into one, either by zipping or combining their emissions.
    private suspend fun combineMultipleFlowsExample() {
        val flowA = flowOf(1, 2, 3)       // Flow emitting integers
        val flowB = flowOf("A", "B", "C") // Flow emitting strings

        // Use combine to get the latest value from both flows
        flowA.combine(flowB) { a, b -> "$a -> $b" }
            .collect { value ->
                Log.d(TAG, "Combined Flow collected: $value")
            }

        // Use zip to pair emissions from both flows by their order
        flowA.zip(flowB) { a, b -> "$a -> $b" }
            .collect { value ->
                Log.d(TAG, "Zipped Flow collected: $value")
            }
    }

    // 12. FlatMapConcat Example
    // flatMapConcat merges the results of multiple flows, emitting each flow's values sequentially.
    private suspend fun flatMapConcatExample() {
        val numbersFlow = flowOf(1, 2, 3)

        // For each number, create a new flow that emits a pair of values (number, number * 2)
        numbersFlow.flatMapConcat { number ->
            flow {
                emit(number)
                emit(number * 2)
            }
        }.collect { value ->
            Log.d(TAG, "FlatMapConcat collected: $value")
        }
    }

    // 13. Conflate Example
    // Conflate drops intermediate values when the consumer cannot keep up with the producer.
    private suspend fun conflateExample() {
        val fastFlow = flow {
            for (i in 1..5) {
                delay(100)   // Simulate a fast producer
                emit(i)
            }
        }

        // Conflate drops intermediate emissions when the collector cannot keep up
        fastFlow.conflate()
            .collect { value ->
                delay(500) // Simulate slow consumer
                Log.d(TAG, "Conflated collected: $value")
            }
    }

    // 14. Terminal Operators Example (first, reduce)
    // Demonstrates terminal operators like first() and reduce() that collect and process data in specific ways.
    private suspend fun terminalOperatorsExample() {
        val numberFlow = flowOf(1, 2, 3, 4)

        // `first()` collects the first emitted value and cancels the flow
        val firstValue = numberFlow.first()
        Log.d(TAG, "First value collected: $firstValue")

        // `reduce()` aggregates the values into a single result
        val sum = numberFlow.reduce { accumulator, value ->
            accumulator + value
        }
        Log.d(TAG, "Sum of all values using reduce: $sum")
    }
}
