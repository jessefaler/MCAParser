# MCAParser  

**MCAParser** is a Java library designed for reading and writing region files ([MCA files](https://minecraft.wiki/w/Region_file_format)). This project uses [ViaNBT](https://github.com/ViaVersion/ViaNBT) to handle NBT (Named Binary Tag) data within MCA files.  

## Features  

- **MCA File Reading:** Supports reading MCA files with data versions 2860 and above (protocol 757).  
- **NBT Parsing:** Efficiently handles NBT tags using the ViaNBT library.  

## Project Status  

⚠️ **Development Paused**  
This project is currently on hold. While the library provides basic functionality for reading MCA files, it:  
- Does not yet support writing MCA files.  
- Has known bugs that may affect functionality.  

If you wish to use this library, you can download the source code and integrate it directly into your project.  

### Alternative Recommendation  
For an up-to-date MCA parsing library in Java, consider using **[ens-gijs/NBT](https://github.com/ens-gijs/NBT)** (under development), a fork of **Querz NBT**. It supports both MCA reading and writing but may require minor modifications to its source code to save written data.  

## Future Plans  

Once development resumes, detailed documentation and instructions will be provided to guide integration and usage. Stay tuned for updates!

---
