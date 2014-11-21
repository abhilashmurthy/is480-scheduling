# IS480 Scheduling System
## Summary
This is a custom built presentation scheduling system for the IS480 (Final Year Project) course in Singapore Management University. The IS480 course is the capstone project for all students in the B.Sc. Information Systems course. Students undertake a project for the duration of one semester under the supervision of a professor. Other faculty members are involved in reviewing the team's progress at certain points in the semester.

Teams need to present at different stages of their project to a panel of faculty members throughout the semester. There are a set of rules determining the faculty members required to attend these presentations. The IS480 scheduling system automates the process of determining these attendees and facilitates the process of finalising the presentation schedule.

The system is currently live [here](http://202.161.45.168/is480-scheduling/welcome) and has around ~160 active users per semester. Key stakeholders/users of the system include the course coordinators, faculty members supervising/reviewing teams, teaching assistants, and students undertaking projects in that semester.

## Technical Details
### System Architecture
- Web application running on an Apache Tomcat instance
- MySQL database running on WAMP
- Java backend, built on Struts2 web application framework
- Bootstrap 2 theme + jQuery powering the front-end
- Quartz Scheduler for triggering cron jobs + timed tasks
- Integration with Gmail to send emails to users

## Team
- Abhilash Murthy
- Dai Xuling
- Prakhar Agarwal
- Targo Gill
- Suresh Subramaniam