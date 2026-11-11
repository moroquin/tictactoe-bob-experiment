# Executive summary

Bob built the same course enrollment system twice with the same prompts: **without-ddd** with the requirements and the architecture only, **with-ddd** with the same plus the DDD documentation (glossary, model, invariants, policies, modules and decision log). Each phase was a new session without memory: F1 build version 1; F2 CR-1 modify and delete sections; F3 CR-2 load a file and reports; F4 CR-3 balanced sections; F5 CR-4 new grade scale; F6 CR-5 prerequisites and credit limit; F7 CR-6 time conflicts.

> One execution per folder: the differences are observations of this execution, not proof. The detail of every number is in `metrics.md`.

## Scoreboard F1–F4

In bold, the best value of each phase. Not bold: a tie or a descriptive metric.

| Metric | F1 without | F1 with | F2 without | F2 with | F3 without | F3 with | F4 without | F4 with |
|---|---|---|---|---|---|---|---|---|
| **Accuracy** | | | | | | | | |
| Hidden scenarios correct | 0/7 | **6/7** | 0/10 | **9/10** | 1/14 | **13/14** | 2/16 | **15/16** |
| Expected lines matched (%) | 89.7 | **99.7** | 90.9 | **99.8** | 92.0 | **99.8** | 92.5 | **99.9** |
| Scenarios of earlier phases still correct | — | — | 0/7 | **6/7** | 0/10 | **9/10** | 1/14 | **13/14** |
| Mutation score without the console (%) | **65** | 56 | **70** | 58 | **74** | 60 | **74** | 60 |
| Bob's tests that pass (%) | 100.0 | 100.0 | 100.0 | 100.0 | 100.0 | 100.0 | 100.0 | 100.0 |
| Bob's suite agrees with the hidden scenarios | no | no | no | no | no | no | no | no |
| Escapes: hidden scenarios that fail with the suite green | 7 | **1** | 10 | **1** | 13 | **1** | 14 | **1** |
| **Agent cost** | | | | | | | | |
| Time (s) | **282** | 542 | **95** | 188 | **234** | 528 | **109** | 123 |
| Context at the end (k tokens) | **57.6** | 108.1 | **66.8** | 74.5 | **96.1** | 120.4 | 69.3 | **49.3** |
| Cost (Bobcoins) | **2.36** | 8.86 | **1.96** | 3.95 | **3.83** | 9.77 | 2.32 | **2.01** |
| Bobcoins per correct hidden scenario | — | 1.477 | — | 0.439 | 3.83 | **0.752** | 1.16 | **0.134** |
| Seconds per correct hidden scenario | — | 90.3 | — | 20.9 | 234.0 | **40.6** | 54.5 | **8.2** |
| **How Bob worked** | | | | | | | | |
| Docs read before the first edit | 2 | 9 | 2 | 8 | 2 | 8 | 2 | 7 |
| Code and test reads before the first edit | 1 | 1 | **6** | 10 | **10** | 18 | 4 | **2** |
| Code reads in the whole session | 0 | 0 | **5** | 8 | **8** | 24 | 1 | 1 |
| Model calls | **28** | 68 | **20** | 38 | **28** | 62 | **21** | 28 |
| **Autonomy** | | | | | | | | |
| Human interventions (messages and rejected approvals) | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| Questions to the user | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| Denied commands it tried | 1 | 1 | **0** | 1 | 1 | 1 | 1 | **0** |
| Result Bob declared | done | done | done | done | done | done | done | done |
| Own decisions | 7 | **6** | **2** | 3 | **4** | 6 | 2 | **1** |
|   · because of gaps or ambiguities in the documentation | **3** | 4 | 1 | 1 | 1 | **0** | 1 | 1 |
| **Design** | | | | | | | | |
| Classes · production lines | 15 · 1096 | 49 · 1617 | 15 · 1170 | 49 · 1688 | 15 · 1366 | 51 · 1984 | 15 · 1367 | 51 · 1986 |
| Lines per file (max) | 416 | **298** | 441 | **318** | 549 | **362** | 549 | **362** |
| Methods with more than 30 lines | **2** | 3 | 3 | 3 | 4 | **3** | 4 | **3** |
| Methods with more than 3 parameters (constructors included) | **16** | 17 | **17** | 19 | **17** | 21 | **17** | 21 |
|   · without constructors | 11 | **9** | 12 | **11** | 12 | **11** | 12 | **11** |
| Ce max (classes the most coupled one uses) | **11** | 29 | **11** | 29 | **11** | 31 | **11** | 31 |
| Propagation cost (%) | 34.2 | **21.3** | 34.2 | **21.3** | 34.2 | **23.0** | 34.2 | **23.0** |
| Yourdon & Constantine index (1 data … 6 content) | 1.39 | 1.39 | 1.43 | **1.4** | 1.43 | **1.4** | 1.43 | **1.4** |
| Worst Yourdon & Constantine connection | external | external | external | external | external | external | external | external |
| TCC median (higher = more cohesive) | 0.17 | **0.78** | 0.17 | **0.78** | 0.17 | **0.83** | 0.17 | **0.83** |
| LCOM* median (lower = more cohesive) | 1.04 | **0.7** | 1.0 | **0.64** | 1.0 | **0.64** | 1.0 | **0.64** |
| Public methods that change state | 18 | **11** | 23 | **12** | 23 | **13** | 23 | **13** |
| Getters that expose modifiable state | 6 | **1** | 7 | **1** | 7 | **1** | 7 | **1** |
| Classes with console I/O | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 |
| **Change** | | | | | | | | |
| New / modified classes | — | — | 0 / 4 | 0 / 5 | 0 / 2 | 2 / 6 | 0 / 1 | 0 / 1 |
| Existing classes modified | — | — | **4** | 5 | **2** | 6 | 1 | 1 |
| Lines changed in existing classes | — | — | **97** | 104 | **246** | 265 | **5** | 11 |
| Existing tests touched | — | — | 1 | 1 | **1** | 2 | **2** | 3 |
| Bobcoins per 100 lines touched | — | — | **2.02** | 3.8 | **1.56** | 2.18 | 46.4 | **18.27** |
| **Language** | | | | | | | | |
| Discarded terms in the code | 1 | **0** | 1 | **0** | 2 | **1** | 2 | **1** |
| Glossary terms present | 30/36 | **36/36** | 30/36 | **36/36** | 33/38 | **38/38** | 33/38 | **38/38** |

## Scoreboard F5–F7

In bold, the best value of each phase. Not bold: a tie or a descriptive metric.

| Metric | F5 without | F5 with | F6 without | F6 with | F7 without | F7 with |
|---|---|---|---|---|---|---|
| **Accuracy** | | | | | | |
| Hidden scenarios correct | 3/17 | **16/17** | 3/19 | **17/19** | 3/21 | **19/21** |
| Expected lines matched (%) | 93.0 | **99.9** | 93.4 | **99.7** | 93.7 | **99.7** |
| Scenarios of earlier phases still correct | 2/16 | **15/16** | 3/17 | **16/17** | 3/19 | **17/19** |
| Mutation score without the console (%) | **75** | 60 | **76** | 63 | **77** | 65 |
| Bob's tests that pass (%) | 100.0 | 100.0 | 100.0 | 100.0 | 100.0 | 100.0 |
| Bob's suite agrees with the hidden scenarios | no | no | no | no | no | no |
| Escapes: hidden scenarios that fail with the suite green | 14 | **1** | 16 | **2** | 18 | **2** |
| **Agent cost** | | | | | | |
| Time (s) | **84** | 89 | **184** | 250 | **165** | 242 |
| Context at the end (k tokens) | 70.9 | **57.3** | **100.5** | 107.1 | 105.8 | **91.2** |
| Cost (Bobcoins) | 2.73 | **2.22** | **4.79** | 7.31 | **4.36** | 5.61 |
| Bobcoins per correct hidden scenario | 0.91 | **0.139** | 1.597 | **0.43** | 1.453 | **0.295** |
| Seconds per correct hidden scenario | 28.0 | **5.6** | 61.3 | **14.7** | 55.0 | **12.7** |
| **How Bob worked** | | | | | | |
| Docs read before the first edit | 2 | 8 | 2 | 8 | 2 | 8 |
| Code and test reads before the first edit | 4 | 4 | 9 | **8** | **6** | 12 |
| Code reads in the whole session | 2 | **1** | **7** | 13 | **4** | 8 |
| Model calls | **23** | 25 | **32** | 54 | **28** | 45 |
| **Autonomy** | | | | | | |
| Human interventions (messages and rejected approvals) | 0 | 0 | 0 | 0 | 0 | 0 |
| Questions to the user | 0 | 0 | 0 | 0 | 0 | 0 |
| Denied commands it tried | 0 | 0 | **1** | 2 | 1 | 1 |
| Result Bob declared | done | done | done | done | done | done |
| Own decisions | **0** | 1 | **3** | 6 | **4** | 5 |
|   · because of gaps or ambiguities in the documentation | 0 | 0 | 1 | **0** | **0** | 1 |
| **Design** | | | | | | |
| Classes · production lines | 15 · 1367 | 51 · 1986 | 15 · 1450 | 52 · 2131 | 15 · 1496 | 52 · 2184 |
| Lines per file (max) | 549 | **362** | 569 | **370** | 615 | **370** |
| Methods with more than 30 lines | 4 | **3** | 4 | 4 | **5** | 6 |
| Methods with more than 3 parameters (constructors included) | **17** | 21 | **17** | 21 | **18** | 21 |
|   · without constructors | 12 | **11** | 12 | **11** | 13 | **11** |
| Ce max (classes the most coupled one uses) | **11** | 31 | **11** | 31 | **11** | 31 |
| Propagation cost (%) | 34.2 | **23.0** | 34.2 | **22.4** | 34.2 | **22.4** |
| Yourdon & Constantine index (1 data … 6 content) | 1.43 | **1.4** | 1.43 | **1.41** | 1.43 | 1.43 |
| Worst Yourdon & Constantine connection | external | external | external | external | external | external |
| TCC median (higher = more cohesive) | 0.17 | **0.83** | 0.17 | **0.83** | 0.17 | **0.83** |
| LCOM* median (lower = more cohesive) | 1.0 | **0.64** | 1.0 | **0.68** | 1.0 | **0.64** |
| Public methods that change state | 23 | **13** | 23 | **13** | 23 | **13** |
| Getters that expose modifiable state | 7 | **1** | 7 | **1** | 7 | **1** |
| Classes with console I/O | 1 | 1 | 1 | 1 | 1 | 1 |
| **Change** | | | | | | |
| New / modified classes | 0 / 2 | 0 / 1 | 0 / 3 | 1 / 9 | 0 / 1 | 0 / 6 |
| Existing classes modified | 2 | **1** | **3** | 9 | **1** | 6 |
| Lines changed in existing classes | 18 | **10** | **130** | 257 | **60** | 94 |
| Existing tests touched | 1 | 1 | 2 | **1** | **1** | 4 |
| Bobcoins per 100 lines touched | **15.17** | 22.2 | 3.68 | **2.8** | 7.27 | **5.97** |
| **Language** | | | | | | |
| Discarded terms in the code | 2 | **1** | 2 | 2 | 2 | 2 |
| Glossary terms present | 33/38 | **38/38** | 34/40 | **40/40** | 34/41 | **40/41** |

## Cumulative cost

| | without-ddd | with-ddd | with-ddd ÷ without-ddd |
|---|---|---|---|
| Time (s) | 1153 | 1962 | 1.70× |
| Cost (Bobcoins) | 22.35 | 39.73 | 1.78× |

Phases included: F1, F2, F3, F4, F5, F6, F7.

## Efficiency: correct scenarios against cost and time

Each phase runs its own hidden scenarios and those of the earlier phases, with the rules of this phase (see `scenarios/EXPECTED.md`). Fewer Bobcoins and seconds per correct scenario = more efficient.

| Phase | Folder | Scenarios correct | Expected lines matched | Bobcoins | Time (s) | Bobcoins per scenario | Seconds per scenario |
|---|---|---|---|---|---|---|---|
| F1 | without-ddd | 0/7 | 315/351 | 2.36 | 282 | — | — |
| F1 | with-ddd | 6/7 | 350/351 | 8.86 | 542 | 1.477 | 90.3 |
| F2 | without-ddd | 0/10 | 429/472 | 1.96 | 95 | — | — |
| F2 | with-ddd | 9/10 | 471/472 | 3.95 | 188 | 0.439 | 20.9 |
| F3 | without-ddd | 1/14 | 584/635 | 3.83 | 234 | 3.83 | 234.0 |
| F3 | with-ddd | 13/14 | 634/635 | 9.77 | 528 | 0.752 | 40.6 |
| F4 | without-ddd | 2/16 | 670/724 | 2.32 | 109 | 1.16 | 54.5 |
| F4 | with-ddd | 15/16 | 723/724 | 2.01 | 123 | 0.134 | 8.2 |
| F5 | without-ddd | 3/17 | 720/774 | 2.73 | 84 | 0.91 | 28.0 |
| F5 | with-ddd | 16/17 | 773/774 | 2.22 | 89 | 0.139 | 5.6 |
| F6 | without-ddd | 3/19 | 817/875 | 4.79 | 184 | 1.597 | 61.3 |
| F6 | with-ddd | 17/19 | 872/875 | 7.31 | 250 | 0.43 | 14.7 |
| F7 | without-ddd | 3/21 | 898/958 | 4.36 | 165 | 1.453 | 55.0 |
| F7 | with-ddd | 19/21 | 955/958 | 5.61 | 242 | 0.295 | 12.7 |
| **Total** | without-ddd | 12/104 | | 22.35 | 1153 | 1.863 | 96.1 |
| **Total** | with-ddd | 95/104 | | 39.73 | 1962 | 0.418 | 20.7 |

## Scalability: what happens while the system grows

A design that scales grows in lines without growing its propagation cost, its coupling or the cost of each change, and without losing correct scenarios.

| Folder | Phase | Lines · files | Lines per file (max) | Methods > 30 lines | Propagation cost (%) | Y&C index | TCC median | Scenarios correct | Bobcoins | Bobcoins per 100 lines touched |
|---|---|---|---|---|---|---|---|---|---|---|
| without-ddd | F1 | 1096 · 15 | 416 | 2 | 34.2 | 1.39 | 0.17 | 0/7 | 2.36 | — |
| without-ddd | F2 | 1170 · 15 | 441 | 3 | 34.2 | 1.43 | 0.17 | 0/10 | 1.96 | 2.02 |
| without-ddd | F3 | 1366 · 15 | 549 | 4 | 34.2 | 1.43 | 0.17 | 1/14 | 3.83 | 1.56 |
| without-ddd | F4 | 1367 · 15 | 549 | 4 | 34.2 | 1.43 | 0.17 | 2/16 | 2.32 | 46.4 |
| without-ddd | F5 | 1367 · 15 | 549 | 4 | 34.2 | 1.43 | 0.17 | 3/17 | 2.73 | 15.17 |
| without-ddd | F6 | 1450 · 15 | 569 | 4 | 34.2 | 1.43 | 0.17 | 3/19 | 4.79 | 3.68 |
| without-ddd | F7 | 1496 · 15 | 615 | 5 | 34.2 | 1.43 | 0.17 | 3/21 | 4.36 | 7.27 |
| with-ddd | F1 | 1617 · 49 | 298 | 3 | 21.3 | 1.39 | 0.78 | 6/7 | 8.86 | — |
| with-ddd | F2 | 1688 · 49 | 318 | 3 | 21.3 | 1.4 | 0.78 | 9/10 | 3.95 | 3.8 |
| with-ddd | F3 | 1984 · 51 | 362 | 3 | 23.0 | 1.4 | 0.83 | 13/14 | 9.77 | 2.18 |
| with-ddd | F4 | 1986 · 51 | 362 | 3 | 23.0 | 1.4 | 0.83 | 15/16 | 2.01 | 18.27 |
| with-ddd | F5 | 1986 · 51 | 362 | 3 | 23.0 | 1.4 | 0.83 | 16/17 | 2.22 | 22.2 |
| with-ddd | F6 | 2131 · 52 | 370 | 4 | 22.4 | 1.41 | 0.83 | 17/19 | 7.31 | 2.8 |
| with-ddd | F7 | 2184 · 52 | 370 | 6 | 22.4 | 1.43 | 0.83 | 19/21 | 5.61 | 5.97 |

- **without-ddd**, from F1 to F7: lines went from 1096 to 1496 (+36 %), the propagation cost from 34.2 % to 34.2 %, the Y&C index from 1.39 to 1.43, correct scenarios from 0 % to 14 %.
- **with-ddd**, from F1 to F7: lines went from 1617 to 2184 (+35 %), the propagation cost from 21.3 % to 22.4 %, the Y&C index from 1.39 to 1.43, correct scenarios from 86 % to 90 %.

With several archived runs, `python3 measure.py stats` writes to `runs/stats.md` the medians, whether each difference repeats in every run, and the correlations between design, accuracy and cost.

## Findings

### F1 · build version 1
**without-ddd**
- Hidden scenarios that fail: F1-01-queries-and-references: line 28: expected '1 | admin | admin', got 'OK: 1 | admin | admin' (queries show the data in creation order; a Section shows the new data of what it refers to); F1-02-session-and-permissions: line 38: expected '800 | New course | 3 | 2', got 'OK: 800 | New course | 3 | 2' (one session at a time, permissions by user type, and the order of the checks (syntax, session, permission, rules)); F1-03-syntax: line 33: expected 'OK', got 'Error: invalid instruction' (what is and what is not a valid instruction (R33–R37, R40)); F1-04-enrollment: line 31: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section of an enrollment, room, grades and unenrolling); F1-05-references-and-deletion: line 45: expected 'G | 12 (75), 105 (3), 109 (40)', got 'OK: G | 12 (75), 105 (3), 109 (40)' (what cannot be deleted while something uses it, keys and capacity); F1-06-users: line 31: expected '6 | Bea | staff', got 'OK: 6 | Bea | staff' (user IDs, names, links to students and the initial admin); F1-07-sections: line 30: expected '8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40', got 'OK: 8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40' (periods, days and references of a new section).
- Gaps Bob had to decide on its own: R24: what constitutes a valid period — must hours be exactly 1–12? / R41: free text after `OK:` for mutating operations — what text? / R35: typographic open-quote `"` — what is the matching close quote?
- Exposed state: model.Classroom.getBuilding() returns model.Building, model.Enrollment.getStudent() returns model.Student, model.Section.getClassroom() returns model.Classroom, model.Section.getCourse() returns model.Course, model.Section.getProfessor() returns model.Professor, service.EnrollmentService.currentUser() returns model.User.
- Discarded terms in the code: CurrentUser (currentUser).

**with-ddd**
- Hidden scenarios that fail: F1-03-syntax: line 38: expected 'Error: invalid instruction', got 'OK: student created' (what is and what is not a valid instruction (R33–R37, R40)).
- Gaps Bob had to decide on its own: `SectionRepository` in `model.puml` had no way to ask "is this student enrolled in this course?", but `EnrollmentService` needs it for I19 (duplicate check), `changeGrades` and `unenroll`. / `Building` in the model had `addClassroom` and `removeClassroom` but not a way to look up one classroom by number (needed by `SectionService` to validate I16 and by `CampusService` to modify/delete). / `AccessService` is constructed with `Session` and `Permissions`, but other services and `EnrollmentService.enrollMe` need to call `requireSession()` and `requirePermission()` on it, and `EnrollmentService.enrollMe` needs to read the logged-in user's student number. / R40 says a number with more than 10 digits is not a valid instruction, but the instruction for `Enroll` takes numbers as arguments. Should classroom numbers that fit in `int` but not a 10-digit `long` be rejected?
- Exposed state: application.AccessService.session() returns domain.access.Session.
- Domain docs updated: decisions.md, model.puml, modules.md.

### F2 · CR-1 modify and delete sections
**without-ddd**
- Hidden scenarios that fail: F1-01-queries-and-references: line 28: expected '1 | admin | admin', got 'OK: 1 | admin | admin' (queries show the data in creation order; a Section shows the new data of what it refers to); F1-02-session-and-permissions: line 38: expected '800 | New course | 3 | 2', got 'OK: 800 | New course | 3 | 2' (one session at a time, permissions by user type, and the order of the checks (syntax, session, permission, rules)); F1-03-syntax: line 33: expected 'OK', got 'Error: invalid instruction' (what is and what is not a valid instruction (R33–R37, R40)); F1-04-enrollment: line 31: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section of an enrollment, room, grades and unenrolling); F1-05-references-and-deletion: line 45: expected 'G | 12 (75), 105 (3), 109 (40)', got 'OK: G | 12 (75), 105 (3), 109 (40)' (what cannot be deleted while something uses it, keys and capacity); F1-06-users: line 31: expected '6 | Bea | staff', got 'OK: 6 | Bea | staff' (user IDs, names, links to students and the initial admin); F1-07-sections: line 30: expected '8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40', got 'OK: 8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40' (periods, days and references of a new section); F2-01-modify-section: line 29: expected '6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40', got 'OK: 6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40' (modifying a section without enrollments, and what it frees); F2-02-sections-with-enrollments: line 33: expected '1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 2/40', got 'OK: 1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 2/40' (a section with enrollments keeps its course and needs room for its students; deleting it needs no enrollments); F2-03-permissions: line 32: expected '6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75', got 'OK: 6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75' (who may modify and delete sections).
- Gaps Bob had to decide on its own: The CR says "the capacity of its new classroom cannot be lower than the number of students enrolled in it" — does "lower than" mean strict less-than, or less-than-or-equal?
- Exposed state: model.Classroom.getBuilding() returns model.Building, model.Enrollment.getSection() returns model.Section, model.Enrollment.getStudent() returns model.Student, model.Section.getClassroom() returns model.Classroom, model.Section.getCourse() returns model.Course, model.Section.getProfessor() returns model.Professor, service.EnrollmentService.currentUser() returns model.User.
- Discarded terms in the code: CurrentUser (currentUser).
- Existing classes modified: Console, Section, EnrollmentService, Store.

**with-ddd**
- Hidden scenarios that fail: F1-03-syntax: line 38: expected 'Error: invalid instruction', got 'OK: student created' (what is and what is not a valid instruction (R33–R37, R40)).
- Gaps Bob had to decide on its own: The CR says enrollments "stay in the section, in the same order" after a modify; does the domain class need to explicitly preserve order?
- Exposed state: application.AccessService.session() returns domain.access.Session.
- Existing classes modified: SectionService, Dispatcher, Section, SectionRepository, InMemorySectionRepository.
- Bob modified REQUIREMENTS.md, which had to stay the same in both folders.
- Domain docs updated: decisions.md, invariants.md, model.puml, modules.md, policies.md.

### F3 · CR-2 load a file and reports
**without-ddd**
- Hidden scenarios that fail: F1-01-queries-and-references: line 28: expected '1 | admin | admin', got 'OK: 1 | admin | admin' (queries show the data in creation order; a Section shows the new data of what it refers to); F1-02-session-and-permissions: line 38: expected '800 | New course | 3 | 2', got 'OK: 800 | New course | 3 | 2' (one session at a time, permissions by user type, and the order of the checks (syntax, session, permission, rules)); F1-03-syntax: line 33: expected 'OK', got 'Error: invalid instruction' (what is and what is not a valid instruction (R33–R37, R40)); F1-04-enrollment: line 31: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section of an enrollment, room, grades and unenrolling); F1-05-references-and-deletion: line 45: expected 'G | 12 (75), 105 (3), 109 (40)', got 'OK: G | 12 (75), 105 (3), 109 (40)' (what cannot be deleted while something uses it, keys and capacity); F1-06-users: line 31: expected '6 | Bea | staff', got 'OK: 6 | Bea | staff' (user IDs, names, links to students and the initial admin); F1-07-sections: line 30: expected '8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40', got 'OK: 8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40' (periods, days and references of a new section); F2-01-modify-section: line 29: expected '6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40', got 'OK: 6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40' (modifying a section without enrollments, and what it frees); F2-02-sections-with-enrollments: line 33: expected '1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 2/40', got 'OK: 1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 2/40' (a section with enrollments keeps its course and needs room for its students; deleting it needs no enrollments); F2-03-permissions: line 32: expected '6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75', got 'OK: 6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75' (who may modify and delete sections); F3-01-load-example: line 2: expected 'OK: load finished, 42 correct and 0 with errors', got 'Error in line 31: invalid instruction' (the load example of CR-2 and its reports); F3-02-load-errors: line 36: expected 'K | 1 (30)', got 'OK: K | 1 (30)' (a load goes on after an error, counts lines from 1 with empty ones, and only accepts creating instructions); F3-04-load-enrollments: line 36: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (enrollments in a loaded file follow the same rules as in the console).
- Gaps Bob had to decide on its own: How should `ClassroomSections` parse the classroom number argument?
- Exposed state: model.Classroom.getBuilding() returns model.Building, model.Enrollment.getSection() returns model.Section, model.Enrollment.getStudent() returns model.Student, model.Section.getClassroom() returns model.Classroom, model.Section.getCourse() returns model.Course, model.Section.getProfessor() returns model.Professor, service.EnrollmentService.currentUser() returns model.User.
- Discarded terms in the code: CurrentUser (currentUser), import (import).
- Existing classes modified: Console, EnrollmentService.

**with-ddd**
- Hidden scenarios that fail: F1-03-syntax: line 38: expected 'Error: invalid instruction', got 'OK: student created' (what is and what is not a valid instruction (R33–R37, R40)).
- Exposed state: application.AccessService.session() returns domain.access.Session.
- Discarded terms in the code: import (import).
- Existing classes modified: Main, ConsoleLoop, Dispatcher, ResponseFormatter, Action, Permissions.
- Bob modified REQUIREMENTS.md, which had to stay the same in both folders.
- Domain docs updated: decisions.md, glossary.md, invariants.md, model.puml, modules.md, policies.md.

### F4 · CR-3 balanced sections
**without-ddd**
- Hidden scenarios that fail: F1-01-queries-and-references: line 28: expected '1 | admin | admin', got 'OK: 1 | admin | admin' (queries show the data in creation order; a Section shows the new data of what it refers to); F1-02-session-and-permissions: line 38: expected '800 | New course | 3 | 2', got 'OK: 800 | New course | 3 | 2' (one session at a time, permissions by user type, and the order of the checks (syntax, session, permission, rules)); F1-03-syntax: line 33: expected 'OK', got 'Error: invalid instruction' (what is and what is not a valid instruction (R33–R37, R40)); F1-04-enrollment: line 31: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section of an enrollment, room, grades and unenrolling); F1-05-references-and-deletion: line 45: expected 'G | 12 (75), 105 (3), 109 (40)', got 'OK: G | 12 (75), 105 (3), 109 (40)' (what cannot be deleted while something uses it, keys and capacity); F1-06-users: line 31: expected '6 | Bea | staff', got 'OK: 6 | Bea | staff' (user IDs, names, links to students and the initial admin); F1-07-sections: line 30: expected '8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40', got 'OK: 8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40' (periods, days and references of a new section); F2-01-modify-section: line 29: expected '6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40', got 'OK: 6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40' (modifying a section without enrollments, and what it frees); F2-02-sections-with-enrollments: line 33: expected '1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 1/40', got 'OK: 1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 1/40' (a section with enrollments keeps its course and needs room for its students; deleting it needs no enrollments); F2-03-permissions: line 32: expected '6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75', got 'OK: 6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75' (who may modify and delete sections); F3-01-load-example: line 2: expected 'OK: load finished, 42 correct and 0 with errors', got 'Error in line 31: invalid instruction' (the load example of CR-2 and its reports); F3-02-load-errors: line 36: expected 'K | 1 (30)', got 'OK: K | 1 (30)' (a load goes on after an error, counts lines from 1 with empty ones, and only accepts creating instructions); F3-04-load-enrollments: line 36: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (enrollments in a loaded file follow the same rules as in the console); F4-01-balanced-sections: line 33: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section with the fewest enrollments among those with room, then the lowest code).
- Gaps Bob had to decide on its own: Should the `r29_fills_next_available_when_first_full` test be renamed to reflect CR-3?
- Exposed state: model.Classroom.getBuilding() returns model.Building, model.Enrollment.getSection() returns model.Section, model.Enrollment.getStudent() returns model.Student, model.Section.getClassroom() returns model.Classroom, model.Section.getCourse() returns model.Course, model.Section.getProfessor() returns model.Professor, service.EnrollmentService.currentUser() returns model.User.
- Discarded terms in the code: CurrentUser (currentUser), import (import).
- Existing classes modified: EnrollmentService.

**with-ddd**
- Hidden scenarios that fail: F1-03-syntax: line 38: expected 'Error: invalid instruction', got 'OK: student created' (what is and what is not a valid instruction (R33–R37, R40)).
- Gaps Bob had to decide on its own: Two existing tests (`I18_full_section_skipped_next_chosen` in `EnrollmentServiceTest` and `I26_courseStudents_returns_enrollments_in_section_then_enrollment_order` in `ConsoleIntegrationTest`) were written for the old "lowest code" rule and now expected wrong sections. Should they be updated?
- Exposed state: application.AccessService.session() returns domain.access.Session.
- Discarded terms in the code: import (import).
- Existing classes modified: SectionChoicePolicy.
- Bob modified REQUIREMENTS.md, which had to stay the same in both folders.
- Domain docs updated: decisions.md, policies.md.

### F5 · CR-4 new grade scale
**without-ddd**
- Hidden scenarios that fail: F1-01-queries-and-references: line 28: expected '1 | admin | admin', got 'OK: 1 | admin | admin' (queries show the data in creation order; a Section shows the new data of what it refers to); F1-02-session-and-permissions: line 38: expected '800 | New course | 3 | 2', got 'OK: 800 | New course | 3 | 2' (one session at a time, permissions by user type, and the order of the checks (syntax, session, permission, rules)); F1-03-syntax: line 33: expected 'OK', got 'Error: invalid instruction' (what is and what is not a valid instruction (R33–R37, R40)); F1-04-enrollment: line 31: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section of an enrollment, room, grades and unenrolling); F1-05-references-and-deletion: line 45: expected 'G | 12 (75), 105 (3), 109 (40)', got 'OK: G | 12 (75), 105 (3), 109 (40)' (what cannot be deleted while something uses it, keys and capacity); F1-06-users: line 31: expected '6 | Bea | staff', got 'OK: 6 | Bea | staff' (user IDs, names, links to students and the initial admin); F1-07-sections: line 30: expected '8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40', got 'OK: 8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40' (periods, days and references of a new section); F2-01-modify-section: line 29: expected '6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40', got 'OK: 6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40' (modifying a section without enrollments, and what it frees); F2-02-sections-with-enrollments: line 33: expected '1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 1/40', got 'OK: 1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 1/40' (a section with enrollments keeps its course and needs room for its students; deleting it needs no enrollments); F2-03-permissions: line 32: expected '6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75', got 'OK: 6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75' (who may modify and delete sections); F3-01-load-example: line 2: expected 'OK: load finished, 42 correct and 0 with errors', got 'Error in line 31: invalid instruction' (the load example of CR-2 and its reports); F3-02-load-errors: line 36: expected 'K | 1 (30)', got 'OK: K | 1 (30)' (a load goes on after an error, counts lines from 1 with empty ones, and only accepts creating instructions); F3-04-load-enrollments: line 36: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (enrollments in a loaded file follow the same rules as in the console); F4-01-balanced-sections: line 33: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section with the fewest enrollments among those with room, then the lowest code).
- Exposed state: model.Classroom.getBuilding() returns model.Building, model.Enrollment.getSection() returns model.Section, model.Enrollment.getStudent() returns model.Student, model.Section.getClassroom() returns model.Classroom, model.Section.getCourse() returns model.Course, model.Section.getProfessor() returns model.Professor, service.EnrollmentService.currentUser() returns model.User.
- Discarded terms in the code: CurrentUser (currentUser), import (import).
- Existing classes modified: Enrollment, EnrollmentService.

**with-ddd**
- Hidden scenarios that fail: F1-03-syntax: line 38: expected 'Error: invalid instruction', got 'OK: student created' (what is and what is not a valid instruction (R33–R37, R40)).
- Exposed state: application.AccessService.session() returns domain.access.Session.
- Discarded terms in the code: import (import).
- Existing classes modified: GradeScale.
- Bob modified REQUIREMENTS.md, which had to stay the same in both folders.
- Domain docs updated: decisions.md, policies.md.

### F6 · CR-5 prerequisites and credit limit
**without-ddd**
- Hidden scenarios that fail: F1-01-queries-and-references: line 28: expected '1 | admin | admin', got 'OK: 1 | admin | admin' (queries show the data in creation order; a Section shows the new data of what it refers to); F1-02-session-and-permissions: line 38: expected '800 | New course | 3 | 2 | prerequisites: none', got 'OK: 800 | New course | 3 | 2 | prerequisites: none' (one session at a time, permissions by user type, and the order of the checks (syntax, session, permission, rules)); F1-03-syntax: line 33: expected 'OK', got 'Error: invalid instruction' (what is and what is not a valid instruction (R33–R37, R40)); F1-04-enrollment: line 31: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section of an enrollment, room, grades and unenrolling); F1-05-references-and-deletion: line 45: expected 'G | 12 (75), 105 (3), 109 (40)', got 'OK: G | 12 (75), 105 (3), 109 (40)' (what cannot be deleted while something uses it, keys and capacity); F1-06-users: line 31: expected '6 | Bea | staff', got 'OK: 6 | Bea | staff' (user IDs, names, links to students and the initial admin); F1-07-sections: line 30: expected '8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40', got 'OK: 8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40' (periods, days and references of a new section); F2-01-modify-section: line 29: expected '6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40', got 'OK: 6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40' (modifying a section without enrollments, and what it frees); F2-02-sections-with-enrollments: line 33: expected '1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 1/40', got 'OK: 1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 1/40' (a section with enrollments keeps its course and needs room for its students; deleting it needs no enrollments); F2-03-permissions: line 32: expected '6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75', got 'OK: 6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75' (who may modify and delete sections); F3-01-load-example: line 2: expected 'OK: load finished, 42 correct and 0 with errors', got 'Error in line 31: invalid instruction' (the load example of CR-2 and its reports); F3-02-load-errors: line 36: expected 'K | 1 (30)', got 'OK: K | 1 (30)' (a load goes on after an error, counts lines from 1 with empty ones, and only accepts creating instructions); F3-04-load-enrollments: line 36: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (enrollments in a loaded file follow the same rules as in the console); F4-01-balanced-sections: line 33: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section with the fewest enrollments among those with room, then the lowest code); F6-01-prerequisites: line 34: expected '777 | compilers1 | 5 | 4 | prerequisites: 771, 732', got 'OK: 777 | compilers1 | 5 | 4 | prerequisites: 771, 732' (prerequisites: cycles, the query format, passing them before enrolling, and deleting courses); F6-02-credit-limit: line 34: expected '732 | Statistics 1 | 4 | 12 | prerequisites: none', got 'OK: 732 | Statistics 1 | 4 | 12 | prerequisites: none' (at most 24 credits, checked when enrolling and when a course raises its credits).
- Gaps Bob had to decide on its own: The CR-5 spec says "passes with 61 or more", but existing code and tests use 65 as the passing grade. Which threshold is canonical?
- Exposed state: model.Classroom.getBuilding() returns model.Building, model.Enrollment.getSection() returns model.Section, model.Enrollment.getStudent() returns model.Student, model.Section.getClassroom() returns model.Classroom, model.Section.getCourse() returns model.Course, model.Section.getProfessor() returns model.Professor, service.EnrollmentService.currentUser() returns model.User.
- Discarded terms in the code: CurrentUser (currentUser), import (import).
- Existing classes modified: Console, Course, EnrollmentService.

**with-ddd**
- Hidden scenarios that fail: F1-03-syntax: line 38: expected 'Error: invalid instruction', got 'OK: student created' (what is and what is not a valid instruction (R33–R37, R40)); F6-01-prerequisites: line 57: expected 'Error in line 2', got 'Error in line 1: invalid instruction' (prerequisites: cycles, the query format, passing them before enrolling, and deleting courses).
- Exposed state: application.AccessService.session() returns domain.access.Session.
- Discarded terms in the code: cap (cap), import (import).
- Existing classes modified: CatalogService, EnrollmentService, Dispatcher, ResponseFormatter, Course, CourseRepository, SectionRepository, InMemoryCourseRepository, InMemorySectionRepository.
- Bob modified REQUIREMENTS.md, which had to stay the same in both folders.
- Domain docs updated: decisions.md, glossary.md, invariants.md, model.puml, modules.md, policies.md.

### F7 · CR-6 time conflicts
**without-ddd**
- Hidden scenarios that fail: F1-01-queries-and-references: line 28: expected '1 | admin | admin', got 'OK: 1 | admin | admin' (queries show the data in creation order; a Section shows the new data of what it refers to); F1-02-session-and-permissions: line 38: expected '800 | New course | 3 | 2 | prerequisites: none', got 'OK: 800 | New course | 3 | 2 | prerequisites: none' (one session at a time, permissions by user type, and the order of the checks (syntax, session, permission, rules)); F1-03-syntax: line 33: expected 'OK', got 'Error: invalid instruction' (what is and what is not a valid instruction (R33–R37, R40)); F1-04-enrollment: line 31: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section of an enrollment, room, grades and unenrolling); F1-05-references-and-deletion: line 45: expected 'G | 12 (75), 105 (3), 109 (40)', got 'OK: G | 12 (75), 105 (3), 109 (40)' (what cannot be deleted while something uses it, keys and capacity); F1-06-users: line 31: expected '6 | Bea | staff', got 'OK: 6 | Bea | staff' (user IDs, names, links to students and the initial admin); F1-07-sections: line 30: expected '8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40', got 'OK: 8 | 12:00am-12:30am | monday | 777 | 109 | G | 47854 | 0/40' (periods, days and references of a new section); F2-01-modify-section: line 29: expected '6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40', got 'OK: 6 | 11:00am-11:50am | thursday | 777 | 109 | G | 47854 | 0/40' (modifying a section without enrollments, and what it frees); F2-02-sections-with-enrollments: line 33: expected '1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 1/40', got 'OK: 1 | 1:00pm-1:50pm | wednesday | 777 | 109 | G | 12445 | 1/40' (a section with enrollments keeps its course and needs room for its students; deleting it needs no enrollments); F2-03-permissions: line 32: expected '6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75', got 'OK: 6 | 11:00am-11:50am | friday | 795 | 12 | G | 12445 | 0/75' (who may modify and delete sections); F3-01-load-example: line 2: expected 'OK: load finished, 42 correct and 0 with errors', got 'Error in line 31: invalid instruction' (the load example of CR-2 and its reports); F3-02-load-errors: line 36: expected 'K | 1 (30)', got 'OK: K | 1 (30)' (a load goes on after an error, counts lines from 1 with empty ones, and only accepts creating instructions); F3-04-load-enrollments: line 36: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (enrollments in a loaded file follow the same rules as in the console); F4-01-balanced-sections: line 33: expected '1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2', got 'OK: 1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/2' (the section with the fewest enrollments among those with room, then the lowest code); F6-01-prerequisites: line 34: expected '777 | compilers1 | 5 | 4 | prerequisites: 771, 732', got 'OK: 777 | compilers1 | 5 | 4 | prerequisites: 771, 732' (prerequisites: cycles, the query format, passing them before enrolling, and deleting courses); F6-02-credit-limit: line 34: expected '732 | Statistics 1 | 4 | 12 | prerequisites: none', got 'OK: 732 | Statistics 1 | 4 | 12 | prerequisites: none' (at most 24 credits, checked when enrolling and when a course raises its credits); F7-01-classroom-and-professor: line 36: expected '4 | 8:20am-9:10am | tuesday | 771 | 10 | E | 12445 | 0/1', got 'OK: 4 | 8:20am-9:10am | tuesday | 771 | 10 | E | 12445 | 0/1' (a classroom and a professor cannot have two overlapping sections; periods that only touch can follow each other); F7-02-student-overlaps: line 38: expected '4 | 7:10am-8:00am | wednesday | 771 | 109 | G | 12445 | 1/40', got 'OK: 4 | 7:10am-8:00am | wednesday | 771 | 109 | G | 12445 | 1/40' (a student is never in two overlapping sections; the choice skips them and a modification cannot create them).
- Exposed state: model.Classroom.getBuilding() returns model.Building, model.Enrollment.getSection() returns model.Section, model.Enrollment.getStudent() returns model.Student, model.Section.getClassroom() returns model.Classroom, model.Section.getCourse() returns model.Course, model.Section.getProfessor() returns model.Professor, service.EnrollmentService.currentUser() returns model.User.
- Discarded terms in the code: CurrentUser (currentUser), import (import).
- Existing classes modified: EnrollmentService.

**with-ddd**
- Hidden scenarios that fail: F1-03-syntax: line 38: expected 'Error: invalid instruction', got 'OK: student created' (what is and what is not a valid instruction (R33–R37, R40)); F6-01-prerequisites: line 56: expected 'Error in line 2', got 'Error in line 1: invalid instruction' (prerequisites: cycles, the query format, passing them before enrolling, and deleting courses).
- Gaps Bob had to decide on its own: CR-6 says "`Section`, `ModifySection` and the `Section` lines of a loaded file respond `Error`" for classroom/professor conflicts. The loaded-file path reuses `SectionService.createSection`/`modifySection`, so no additional change is needed.
- Exposed state: application.AccessService.session() returns domain.access.Session.
- Discarded terms in the code: cap (cap), import (import).
- Existing classes modified: EnrollmentService, SectionService, Period, SectionChoicePolicy, SectionRepository, InMemorySectionRepository.
- Bob modified REQUIREMENTS.md, which had to stay the same in both folders.
- Domain docs updated: decisions.md, glossary.md, invariants.md, model.puml, modules.md, policies.md.

## How to read the metrics

- **Hidden scenarios:** sequences of instructions Bob never sees (`scenarios/`). The reference model `oracle.py` computes what each one must output in each phase; a change request can change that, and `scenarios/EXPECTED.md` shows where. A scenario is correct when every line matches; the text after `OK:` and `Error:` is free unless the requirements fix it. **Expected lines matched** counts the expected lines that appear in order, so a nearly correct output still scores.
- **Scenarios of earlier phases:** they run in every later phase with the rules of that phase: some check that nothing broke, others that an old behaviour changed as the change request said.
- **Ce (efferent coupling):** how many project classes a class uses. Lower is better.
- **Ca (afferent coupling):** how many classes use it. It is only fine to be high for stable classes, such as a value object.
- **Propagation cost:** for each change in a class, what part of the system it can reach, directly or transitively (MacCormack et al., 2006). Lower is better; if it grows with each phase, the design scales badly.
- **Yourdon & Constantine:** the type of connection between classes, from best to worst: 1 data (simple values), 2 stamp (passes a whole object), 3 control (passes a boolean or enum flag that decides what the other one does), 4 external (they share the console or another device), 5 common (they share mutable global data), 6 content (one touches the other's fields). The index is the mean per class pair.
- **TCC and LCC:** share of pairs of visible methods that share a field, directly (TCC) or through a chain (LCC). 1 is very cohesive; TCC < 0.5, not very cohesive.
- **LCOM\*:** lack of cohesion. 0 means every method uses every field; 1 or more, that the class mixes responsibilities. Getters are excluded.
- **Size:** lines per file and per method without blank lines or comments; a method with more than 30 lines or more than 3 parameters usually does too much.
- **Efficiency:** Bobcoins and seconds per correct hidden scenario. **Bobcoins per 100 lines touched** (F2 onwards) is the marginal cost of the change.
- **Mutation score:** PIT introduces small bugs in the code and counts how many the tests detect. It is shown without the console classes.
- **Encapsulation:** the public methods that change state are the doors to modify the data; a getter that exposes modifiable state lets callers skip the rules.
- **Open/Closed:** a well-absorbed change adds classes and touches few lines of the existing ones. A change request that only changes a rule (F4, F5) should touch as few classes as possible.
- **Context:** what filled Bob's window at the end, not the total consumed; the cost in Bobcoins reflects the spend better.
