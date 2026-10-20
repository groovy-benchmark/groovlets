/*
 * Th efollowing variables will be injected and available for use
 * in the template:
 *  md : the metadata returned by the service
 *  id : the serviceId of the service
 */

def atts = [
	CMDVersion: '1.2',
	'xmlns:xsi':"http://www.w3.org/2001/XMLSchema-instance",
	'xmlns:cmd':"http://www.clarin.eu/cmd/1",
	'xmlns:cmdp':"http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1311927752306",
	'xsi:schemaLocation':"http://www.clarin.eu/cmd/1 http://www.clarin.eu/cmd/1/xsd/cmd-envelop.xsd http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1311927752306 https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1311927752306/1.2/xsd"
]
xml.'cmd:CMD'(atts) {
	'cmd:Header' {
		'cmd:MdCreator' md.vendor
		'cmd:MdCreationDate' new Date().format('yyyy-MM-dd')
		'cmd:MdProfile' 'What goes here?'
		'cmd:MdCollectionDisplayName' 'Language Applications Grid'
	}
	'cmd:Resources' {
		'cmd:ResourceProxyList' {
		    'cmd:ResourceProxy' {
		        id id
		        'cmd:ResourceType' 'Resource'
		        'cmd:ResourceRef' {
		            p 'A reference to the file represented by this &lt;cmd:ResourceProxy>, in the form of a PID or a URL.'
		            p 'I am not sure what this means. What is "the file represented by this cmd:ResourceProxy?"'
		        }
		    }
		}
		'cmd:JournalFileProxyList' {
		    p 'What is this?'
		    'cmd:JournalFileProxy' {
		        'cmd:JournalFileProxyRef' 'Can this just be the URL to the api.lappsgrid.org/metadata for the service?'
		    }
		}
		'cmd:ResourceRelationList' {
		    p 'What is this?'
		}
	}
	'cmd:IsPartOfList' {
	    p "I don't think we need this?"
	}
	'cmd:Components' {
	    p "I think this will be boilerplate for most LAPPS services."
	}
}
