# Assignment 8 (Kandice Wu and Ally Rogers)
Code for the 2019 Skip Lists assignment

## Complexity Analysis
### Data discussion
As the data below shows, for increasing skip list lengths, the cost per call to one of the core operations decreases, meaning that the methods operate in logarithmic time O(logn). This is illustrated by the decrease in the List Size / Average # of calls as the list length increases.

### Data (output from terminal):
-----------------------
List length 500
-----------------------
Average # of calls:
 * Set = 19933
 * Get = 17874
 * Remove = 7500

List Size / Average # of calls:
 * Set = 0.025084031505543572
 * Get = 0.02797359292827571
 * Remove = 0.06666666666666667

-----------------------
List length 1000
-----------------------
Average # of calls:
 * Set = 43472
 * Get = 39528
 * Remove = 15000

List Size / Average # of calls:
 * Set = 0.023003312476996687
 * Get = 0.02529852256628213
 * Remove = 0.06666666666666667

-----------------------
List length 2000
-----------------------
Average # of calls:
 * Set = 91681
 * Get = 83689
 * Remove = 30000

List Size / Average # of calls:
 * Set = 0.021814770781296015
 * Get = 0.023898003321822462
 * Remove = 0.06666666666666667

-----------------------
List length 4000
-----------------------
Average # of calls:
 * Set = 199273
 * Get = 183319
 * Remove = 60000

List Size / Average # of calls:
 * Set = 0.020072965228605983
 * Get = 0.021819887736677594
 * Remove = 0.06666666666666667

-----------------------
List length 8000
-----------------------
Average # of calls:
 * Set = 426170
 * Get = 394335
 * Remove = 120000

List Size / Average # of calls:
 * Set = 0.018771851608513034
 * Get = 0.02028731915756907
 * Remove = 0.06666666666666667

-----------------------
List length 16000
-----------------------
Average # of calls:
 * Set = 864927
 * Get = 800643
 * Remove = 248000

List Size / Average # of calls:
 * Set = 0.01849867098610634
 * Get = 0.019983937909904916
 * Remove = 0.0645161290322580
