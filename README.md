# Low Level Java Utils for Game Dev
 Low Level Utility classes in Java created by me while making memory optimized games and simulations. Most have very specific use cases and are intended for console applications.
 
 Contains:
  •RasterTools
    ToRaster:
      args
     String: filePath, file path to source
      returns
     byte [][][]: raster, multidimensional array of pixels arranged in x-y coordinates
      and split between 3 color channels
    writeRasterToImage:
      args
     byte [][][]: raster, modified raster array to be written to memory
     String: filePath, original image loaded into raster from memory (used for matching header data and dimensionality)
     String: outputPath, output path in memory where file should be stored
      returns
     void
