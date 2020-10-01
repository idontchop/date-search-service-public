# date-search-service
Reactive Search Service for Dating App

* As of April 15, 2020, additional development on this project will go into a private repository. Though still at low-fidelity,
the basic structure can be easily inferred and a working app is very close.
see: https://github.com/idontchop/date-search-service-public/blob/master/src/main/java/com/idontchop/datesearchservice/api/SearchPotentialsApi.java

# Micro Service Architecture
![Dating App Microservice Architecture](https://github.com/idontchop/date-search-service-public/blob/master/src/main/resources/architecture%20uml-01.jpg)

# Dating App Microservice Architecture
This project is part-learning project, part-job application, part-maybe future business endeavor. In the design, I wanted to accommodate the following business requirements from the ground up.
*Highly-flexible Search
*Modern Pay Structure
*Scalable to millions of users from opening

# Highly-flexible Search
The search service would need to be stateless and database-free to allow for horizontal scalability. Every search-able option would reside on a Microservice whose only purpose was the serve the data.
This allows the search-service to provide the user expensive calculations.
For example, a search in a certain location, age group, and non-smoker/non-drinking/no-children would need to search the following:
1. Find recent pings at the location (the base search)
2. Asynchronously run the results through other microservices as a filter
	1. Age
	2. Gender
	3. Profile (smokign/drinking/children)
	4. Blocks (make sure not to show blocks)
	5. Hides
3. Additionally arrange the results based on AI of the user.

See the following for work completed for this demonstration:

https://github.com/idontchop/date-search-service-public/blob/master/src/main/java/com/idontchop/datesearchservice/api/SearchPotentialsApi.java

# Modern Pay Structure

In-Design

# Scalable to millions of users from opening

The following services deployed to allow horizontal scaling on each services, short-circuiting, SSO Authentication

1. Spring Eureka
2. Spring Cloud Gateway
