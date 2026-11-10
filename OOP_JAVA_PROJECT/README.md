# Clinic Appointment System — automatic IDs and Return

Updated from your Java files supplied on 28 October 2026.

## Installation in VS Code

1. Back up your current project.
2. Copy this package's src folders into your existing src folder, replacing matching files. Include the NEW src/service/IdGenerator.java. The complete set contains 17 application classes and one optional test class.
3. Keep your current patients.txt, doctors.txt, appointments.txt and schedules.txt in the project root. This package intentionally contains no replacement clinic data.
4. Save all files. Use Ctrl+Shift+P → Java: Clean Java Language Server Workspace, then allow a rebuild if VS Code still shows old behavior.
5. Run project.Main with the project root as its working directory. Existing module-info.java may stay; every new class has a named package and uses java.base only.

id_sequences.txt is created automatically on Save Data or normal Exit. Keep it alongside the four clinic data files and include it when backing up or moving the project. Do not create an empty replacement over an existing counter file.

## Automatic IDs

- Doctors: DOC-519 → DOC-520
- Patients: PNT-018 → PNT-019
- Appointments: APT-012 → APT-013
- Schedules: SCH-009 → SCH-010, unique across all doctors

Each series starts at 001 when neither records nor a saved counter provide a higher value. The next ID is based on the highest numeric ID, not file order or the number of records. Existing loaded records update counters, and saved higher counters are retained after deletion. 999 advances to 1000; numbers are not limited to three digits.

The ID is displayed when starting a new form. It is committed only after successful insertion; return, invalid input that aborts a form, and rejected bookings do not consume it. Duplicate validation remains at the service boundary, including case-insensitive comparisons and trimmed surrounding spaces. Schedule insertion through DoctorService rejects IDs used by another doctor, and file loading checks cross-doctor duplicates too.

Existing legacy identifiers such as SCH-511-MON are preserved. They do not belong to the new SCH-number sequence. If only legacy schedules exist, the first new schedule is SCH-001. Patient, doctor, appointment and schedule IDs already in your files are not renumbered.

Counters and records persist on manual Save or normal Exit, as before. An abrupt shutdown can lose unsaved changes. The counter file is replaced via a temporary file; the clinic's multiple data-file saves are still not a single transaction. This remains a single-process application, not a concurrency-safe server.

## Return command

Type return (any letter case, optional surrounding spaces) at any form field to cancel the entire current operation and return to its menu. This works at text fields, IDs selecting existing records, age, dates and schedule times. No partial add/update/delete is performed.

Example:

Doctor Management → 6 Add Doctor Schedule
Doctor ID: [return = cancel] return
Operation cancelled. Returning to the menu.

At a submenu's number selection, return acts like 0 Back. At the main menu, return acts like 0 Save and Exit. Search now remains in its submenu until 0 or return is selected. End-of-input safely cancels a form and exits through the menus instead of throwing an uncaught scanner error.

Updating, searching, deleting, cancelling, completing and rescheduling still ask for the existing record ID. Adding a schedule still asks for its doctor ID. Booking still asks for patient and doctor IDs. Only the identity of the NEW record is generated.

## Code guide

- Main.java: creation-flow IDs, reusable cancellable input helpers, menu handling and sequence load/save calls.
- IdGenerator.java: numeric high-water marks, preview without consumption, and counter persistence.
- PatientService.java / DoctorService.java / AppointmentService.java: next-ID methods, successful-insert tracking and duplicate safeguards.
- DoctorService.java: globally unique schedule insertion.
- Person.java / Appointment.java: normalize identity whitespace.
- FileManager.java: duplicate schedule validation during loading and invalid-doctor row recovery.

Comments identify the generation, insertion, persistence, cancellation and navigation blocks. Other supplied application files are included unchanged for a complete source package.

## Optional focused tests

Run src/testing/TestAutoIds.java using VS Code's Run Java action. It launches its tests in an isolated temporary directory, so it does not overwrite your data or counters. It checks ID generation, duplicates, legacy compatibility and saved-counter behavior.

Local verification on OpenJDK 17:
- 16 focused Java checks passed.
- 45 scripted console checks passed, including cancellation at every field of add/update/search/delete/status flows, generation for all four entities, restart after deleting the highest doctor ID, and EOF recovery.
- All 34 existing Phase 5 regression cases passed using isolated copies of the previous schedule fixture.

Verification details are in LOCAL_VERIFICATION.txt. These are local results; rerun the application on your Windows installation before submitting new test evidence. The earlier DOCX reports have not been changed by this source-code update.
