/*-
 * #%L
 * Use deep learning frameworks from Java in an agnostic and isolated way.
 * %%
 * Copyright (C) 2022 - 2023 Institut Pasteur and BioImage.IO developers.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.bioimage.modelrunner.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;


/**
 * Class to unzip files and directories
 * 
 * @author Carlos Javier Garcia Lopez de Haro
 */

public class ZipUtils
{
	
	/**
	 * Unzip a zip file into the wanted path
	 * @param sourcePath
	 * 	path to the zip file
	 * @param targetPath
	 * 	path to the file where everything will be extracted
	 * @throws IOException if there is any error extracting the files
	 */
    public static void unzipFolder(String sourcePath, String targetPath) throws IOException {
 	    
    	FileInputStream fis = new FileInputStream(new File(sourcePath));
    	ZipInputStream zis = new ZipInputStream(fis);

        ZipEntry entry = zis.getNextEntry();

        while (entry != null) {

            File file = new File(targetPath, entry.getName());
            

            // Check if entry is directory (if the entry name ends with '\' or '/'
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                File parent = file.getParentFile();

                if (!parent.exists()) {
                    parent.mkdirs();
                }

                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {

                    int bufferSize = Math.toIntExact(entry.getSize());
                    byte[] buffer = new byte[bufferSize > 0 ? bufferSize : 4096];
                    int location;
                    
                    while ((location = zis.read(buffer)) != -1 && !Thread.interrupted()) {
                        bos.write(buffer, 0, location);
                    }
     	           bos.close();
                } catch (ZipException e) {
    		 	    zis.close();
    		 	    fis.close();
    				throw e;
    			}
            }
            entry = zis.getNextEntry();
        }
 	    zis.close();
 	    fis.close();
 	}
}
