# PricingEngine
A take-home OO oriented interview problem. I did not use any of Java8 features except to convert Map to List since the instructions 
emphasized OO. For extensive use of the functional-like features of Java 8, see the HiveUnit project.

Instructions:
How we evaluate your code
We will be looking at a number of things including the design aspect of your solution and your object oriented programming skills. 
Whilst these are small problems, we expect you to submit what you believe is “production-quality” code that you would be able to run,
maintain and evolve. You don’t need to “gold plate” your solution, but we are looking for something more than a bare-bones algorithm.
You should submit code that you would be happy to produce in a real project, or that you would be happy to receive from a colleague. We recommend you to provide adequate tests to indicate full code coverage. We also recommend you follow good code hygiene and clean code practices to reduce cyclomatic complexity of your classes.

Statement of the problem:

An online retail company conducts market research to competitively price their products.
Surveyed data contains Product code, Competitor and Price.
 
The retail company uses a Pricing engine which recommends most frequently occurring price. If multiple prices occur frequently, 
the least amongst them is chosen.
 
Products are classified based on parameters like Supply, Demand. Possible values are Low (L), High (H)
 
If Supply is High and Demand is High, Product is sold at same price as chosen price.
If Supply is Low and Demand is Low, Product is sold at 10 % more than chosen price.
If Supply is Low and Demand is High, Product is sold at 5 % more than chosen price.
If Supply is High and Demand is Low, Product is sold at 5 % less than chosen price.
 
Prices less than 50% of average price are treated as promotion and not considered.
Prices more than 50% of average price are treated as data errors and not considered.
(The above requirement seems wrong since it allows only prices exactly equal to 50% of 
average and the expected output in the spec is not consistent with that rule. I interpreted 
that as a typo and used 150% of the average as the indicator of data errors).

Input consists of number of products, followed by each Product's supply and demand parameters.
followed by number of surveyed prices, followed by competitor prices.
 
Output must be recommended price for each product.
 
Input 1:
2
flashdrive H H
ssd L H
5
flashdrive X 1.0
ssd X 10.0
flashdrive Y 0.9
flashdrive Z 1.1
ssd Y 12.5
 
Output 1:
A 0.9
B 10.5
 
Input 2:
2
mp3player H H
ssd L L
8
ssd W 11.0
ssd X 12.0
mp3player X 60.0
mp3player Y 20.0
mp3player Z 50.0
ssd V 10.0
ssd Y 11.0
ssd Z 12.0
 
Output 2:
A 50.0
B 12.1
