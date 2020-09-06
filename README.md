# BankService
Web service for managing banks and their branches. (Spring Boot, Hibernate, Angular 7, MySQL)

## Overview
The service is a single page application (SPA) with both front end and back end validation.<br>

## Functionalities:
* Search banks and branches (by name, swift code, bank code, branch code, valid on date)
* Adding new banks and branches
* Updating bank and branch info
* Invalidating banks and branches

## Backend response:
Every backend response is formatted as follows:<br>
{<br>
&nbsp;&nbsp;"data":...,<br>
&nbsp;&nbsp;"messages": [<br>
&nbsp;&nbsp;&nbsp;&nbsp;{<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"code": ...,<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"message": ...,<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"severity": ...<br>
&nbsp;&nbsp;&nbsp;&nbsp;}<br>
&nbsp;&nbsp;]<br>
}<br>

## Validation:
* valid_to must be after today and valid_from<br>
  (if valid_to is specified, time is automatically set to be 11:59 PM -  the end of the day)
* bank_code must be unique, 2 banks with the same code can exist but must be valid within different time periods
* bank swift_code must be unique, 2 banks with the same code can exist but must be valid within different time periods
* branch swift_code must be unique during a valid period between branches of different banks <br>
(branches can have the same swift_code if they belong to the same bank)
* banks and branches can be invalidated only if valid_to is in the future from now<br>
(if valid_from is in the future, valid_to is set to be 1 second after valid_from,<br>
otherwise valid_to is set to today)
* invalidating a bank automatically invalidates its branches

## Sidenote
This project was an assignment as part of the course - Advanced Java programming during the 7th semester at the Faculty of Computer Science in Belgrade.<br>
All platform functionalities were defined in the assignment specifications.

## Contributors
- Stefan Ginic - <stefangwars@gmail.com>
