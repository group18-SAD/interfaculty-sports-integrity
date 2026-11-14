# Interfaculty Sports Integrity Anomaly Detector for KIU

A desktop application that stores interfaculty sports results and flags matches with unusual patterns, so the sports committee can review them. It is a decision-support tool: a flag is a reason to investigate, not proof of wrongdoing.

Group 18, System Analysis and Design, Bachelor of Information Technology, Kampala International University.

## Features

- Results entry with home and away lineups, referee or umpire, and forfeits
- Eligibility check: each lineup is compared with the faculty roster
- Five anomaly rules run on every saved result (R1 to R5)
- Flag review with statuses: Flagged, Under Investigation, Cleared, Penalized
- Audit trail of result edits, lock changes and status changes
- Dashboard with totals by status, rule and sport
- Role-based access: CLERK and ADMIN

## Technology

- Java with Swing (desktop interface)
- MySQL database, run through XAMPP
- MySQL Connector/J (JDBC driver)
- NetBeans IDE and Apache Ant build

## Setup

1. Install XAMPP and start **Apache** and **MySQL**.
2. Open `http://localhost/phpmyadmin`, click **SQL**, and run the database script in the project folder (`sports_integrity.sql`). It creates the `sports_integrity` database with its tables and starting data.
3. Create a file named `db.properties` in the project folder, next to the `src` folder, with:

   ```properties
   db.url=jdbc:mysql://localhost:3306/sports_integrity?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=
   ```

   This file holds your database login, so it is not included in this repository.
4. Open the project in NetBeans. Under **Libraries**, add `mysql-connector-j-x.x.x.jar`.
5. Run **Clean and Build**, then **Run**.
6. The first time, create the administrator account in the sign-in window (password at least 8 characters).

## Using the system

-Results: enter a fixture, scores, referee and lineups, then click **Save result and run checks**.
-Edit Results: correct a result (a reason is required). Administrators can lock and unlock results.
-Eligibility: add players to a faculty roster and check a fixture's lineups.
-Flags & Status: review flags. Administrators can change a status and add a note.
-Dashboard:season totals.
Settings (administrators only):margin thresholds per sport and user accounts.

 ## Anomaly rules

Rule and What it flags 
R1-The same two faculties meet at least 3 times in a sport, and one always wins |
R2-Score margin greater than the sport threshold (Football 3, Netball 15, Basketball 25, provisional) |
R3-A lineup player who is not on the faculty roster |
R4-A faculty that forfeits at least 2 results in a sport |
R5-A referee with at least 3 decided results, all won by the same faculty |

Thresholds are provisional until agreed with the sports office.

## Project structure

- `src/sportsintegrity/` the Java source
- `nbproject/` NetBeans project settings
- `build.xml` and `manifest.mf` Ant build files
- `sports_integrity.sql` database script (add it to the repository, or keep it in the Drive folder)

## Limitations

- Thresholds are not yet agreed.
- No password reset is available yet.
- Not yet tested: R1 and R3 to R5, and the clerk permission checks.
