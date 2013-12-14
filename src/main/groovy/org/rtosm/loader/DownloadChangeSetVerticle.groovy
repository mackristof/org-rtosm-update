package org.rtosm.loader

import java.awt.Polygon;

import org.joda.time.DateTime
import org.vertx.groovy.platform.Verticle

import groovy.json.JsonBuilder
import groovy.util.Node



/**
 * Verticle listen to "download-changeset-by-id" and then search for details and then publish details in GeoJson Format 
 * @author https://github.com/mackristof
 *
 */
class DownloadChangeSetVerticle extends Verticle {
	def start() {
		vertx.eventBus.registerHandler("download-changeset-by-id"){ message ->
			container.logger.info("download-changeset-by-id received msg : $message.body")
			GString url = "http://api.openstreetmap.org/api/0.6/changeset/$message.body"
			Node osmChange = new XmlParser().parse(url)
			osmChange.changeset.findAll {it.'@max_lat'!=null }.each { changeset ->
				JsonBuilder builder = new groovy.json.JsonBuilder()
				def root = builder {
					type 'Feature'
					properties {
						id (changeset.'@id').toString() 
						datechangeset (changeset.'@created_at').toString() 
						comments (changeset.tag.find { it.'@k' == 'comment' }?.'@v' ).toString() 
					}
					geometry {
						type 'Point'
						coordinates ([(changeset.'@min_lon'.value).toString(), (changeset.'@min_lat'.value).toString()])
					}	
				}
				vertx.eventBus.publish("get-changeset",builder.toString())

			}
            message.reply(osmChange.changeset.'@id'.toString())
		}
	}

}
