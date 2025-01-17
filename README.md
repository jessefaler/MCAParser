---
title: "MCAParser"
description: |
  MCAParser is a Java library designed for reading and writing region files (MCA files).
  This project leverages ViaNBT to handle NBT (Named Binary Tag) data within MCA files.

sections:
  - Features:
      - MCA File Reading: Supports reading MCA files with data versions 2860 and above (protocol 757).
      - NBT Parsing: Efficiently handles NBT tags using the ViaNBT library.
  - Project Status:
      note: |
        Development Paused
        This project is currently on hold. While the library provides basic functionality for reading MCA files, it:
        - Does not yet support writing MCA files.
        - Has known bugs that may affect functionality.
      alternative_recommendation: |
        For those seeking a robust and up-to-date MCA parsing library in Java, consider using ens-gijs NBT, a fork of Querz NBT.
        It supports both MCA reading and writing but may require minor modifications to its source code to save written data.
  - Future Plans:
      note: |
        Once development resumes, detailed documentation and instructions will be provided to guide integration and usage.
        Stay tuned for updates!
