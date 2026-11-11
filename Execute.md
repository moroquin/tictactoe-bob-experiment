# One run, command by command

```bash
scripts/init.sh                          # first time only; later runs: scripts/new_run.sh "description" -y
```

For each phase, F1 to F7, in order:

1. Open `bob` in `../without-ddd` and in `../with-ddd` (a new session each, side by side) and paste the phase prompt as is:
   F1 `prompts/F1-build.md` · F2 `prompts/F2-cr1-modify-delete-sections.md` · F3 `prompts/F3-cr2-load-and-reports.md` ·
   F4 `prompts/F4-cr3-balanced-enrollment.md` · F5 `prompts/F5-cr4-new-grade-scale.md` ·
   F6 `prompts/F6-cr5-prerequisites-and-credits.md` · F7 `prompts/F7-cr6-time-conflicts.md`
2. When both sessions finish:
   ```bash
   python3 measure.py snapshot F<n>
   python3 measure.py audit
   ```

After F7:

```bash
python3 measure.py                       # measures the seven phases
# optional: bob in this repository, "run COMPARE.md"
python3 presentation.py
scripts/new_run.sh "what with-ddd received in this run" -y
```
