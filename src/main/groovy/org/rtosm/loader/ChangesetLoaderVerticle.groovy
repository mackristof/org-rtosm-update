package org.rtosm.loader



import java.util.zip.GZIPInputStream;

import org.joda.time.DateTime
import org.vertx.groovy.platform.Verticle


/**
 * Verticle listen to "load-changesets" and then search Changesets and reply ChangeSets for the last minute
 * @author christophem
 *
 */
class ChangesetLoaderVerticle extends Verticle {

	def start() {
		container.logger.info("ChangesetLoaderVerticle started")
		vertx.eventBus.registerHandler("load-changesets"){ message ->
			container.logger.debug("received msg load-changesets : $message.body")
			search(message)
		}
		
	}

	def search(message) {
		def logger = container.logger
		"http://planet.openstreetmap.org/replication/minute/state.txt".toURL().eachLine { line ->
			if (line =~ /sequenceNumber/) {
				String seq = line.split("=")[1]
				seq = ('0'*(9-seq.size()))+seq
				File fileZip = File.createTempFile("temp",".zip")
				logger.info("http://planet.openstreetmap.org/replication/minute/${seq[0..2]}/${seq[3..5]}/${seq[6..8]}.osc.gz")
				use (FileBinaryCategory) {
					fileZip << "http://planet.openstreetmap.org/replication/minute/${seq[0..2]}/${seq[3..5]}/${seq[6..8]}.osc.gz".toURL()
				}
				File file = File.createTempFile("temp",null)
				decompressGzipFile(fileZip.getAbsolutePath(), file.getAbsolutePath())
				logger.info(file.getAbsolutePath())
				groovy.util.Node osmChange = new XmlParser().parse(file)
				// list all changesets in modifications
				List changeSets = osmChange.'**'.findAll{ it.'@changeset'!=null}.'@changeset'.unique(false)
				logger.info('changesets.size()='+changeSets.size())
				fileZip.delete()
				file.delete()
				message.reply(['ids':changeSets])
				
			}
		}
	}

	private static void decompressGzipFile(String gzipFile, String newFile) {
		try {
			FileInputStream fis = new FileInputStream(gzipFile);
			GZIPInputStream gis = new GZIPInputStream(fis);
			FileOutputStream fos = new FileOutputStream(newFile);
			byte[] buffer = new byte[1024];
			int len;
			while((len = gis.read(buffer)) != -1){
				fos.write(buffer, 0, len);
			}
			//close resources
			fos.close();
			gis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}




class FileBinaryCategory{
	def static leftShift(File file, URL url){
		url.withInputStream {is->
			file.withOutputStream {os->
				def bs = new BufferedOutputStream( os )
				bs << is
			}
		}
	}
}