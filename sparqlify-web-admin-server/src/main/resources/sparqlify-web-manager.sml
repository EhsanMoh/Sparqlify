/**
 * Sparqlification Mapping Language file for the Sparqlify Admin schema
 *
 *
 * Note: Of course we don't want to publish the passwords
 *
 */

Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
Prefix owl: <http://www.w3.org/2002/07/owl#>
Prefix xsd: <http://www.w3.org/2001/XMLSchema#>
Prefix o: <http://example.org/ontology/>
Prefix r: <http://example.org/resource/>


Create View jdbcDataSource As
  Construct {
    ?s
      a o:JdbcDataSource ;
      o:id ?i ;
      rdfs:label ?l ;
      rdfs:comment ?c ;
      o:jdbcUrl ?ju ;
      o:username ?u ;
      .
  }
  With
    ?s = uri(r:, 'jdbcDataSource', ?id)
    ?i = typedLiteral(?id, xsd:int)
    ?l = plainLiteral(?primarylabel)
    ?c = plainLiteral(?primarycomment)
    ?ju = uri(?jdbcurl)
    ?u = plainLiteral(?username)
  From
    "jdbcdatasource"

    
Create View textResource As
  Construct {
    ?s
      a o:TextResource ;
      o:id ?i ;
      rdfs:label ?l ;
      rdfs:comment ?c ;
      o:type ?t ;
      o:format ?f ;
      o:data ?d ;
      .
  }
  With
    ?s = uri(r:, 'textResource', ?id)
    ?i = typedLiteral(?id, xsd:int)
    ?l = plainLiteral(?primarylabel)
    ?c = plainLiteral(?primarycomment)
    ?t = plainLiteral(?type)
    ?f = plainLiteral(?format)
    ?d = plainLiteral(?data)
  From
    "textresource"

    
Create View rdb2rdfConfig As
  Construct {
    ?s
      a o:Rdb2RdfConfig ;
      o:id ?i ;
      o:contextPath ?p ;
      o:dataSource ?d ;
      o:resource ?r ;
      .
  }
  With
    ?s = uri(r:, 'rdb2RdfConfig', ?id)
    ?i = typedLiteral(?id, xsd:int)
    ?p = plainLiteral(?contextpath)
    ?d = uri(r:, 'jdbcDataSource', ?jdbcdatasource_id)
    ?r = uri(r:, 'textResource', ?textresource_id)
  From
    "rdb2rdfconfig"
    

/*
Create View rdb2rdfExecution As
  Construct {
    ?s
      a o:Rdb2RdfExecution ;
      a o:ServiceExecution ;
      o:id ?i ;
//      o:config ?c ;
      o:status ?st ;
      .
  }
  With
    ?s = uri(r:, 'serviceExecution', ?id)
    ?i = typedLiteral(?id, xsd:int)
//    ?c = uri(r:, 'rdb2RdfConfig', ?config_id)
    ?st = plainLiteral(?status)
  From
    [[SELECT id, status FROM configtoexecution WHERE configclassname='org.aksw.sparqlify.admin.model.Rdb2RdfConfig']]

*/

// Note: Actually this view maps the execution context (i.e. the execution's associated persistence entity), not the execution itself
Create View rdb2rdfExecution As
  Construct {
    ?s
      a o:Rdb2RdfExecution ;
      a o:ServiceExecution ;
      o:id ?i ;
      o:config ?c ;
      o:status ?st ;
      .
  }
  With
    ?s = uri(r:, 'rdb2RdfExecution', ?id)
    ?i = typedLiteral(?id, xsd:int)
    ?c = uri(r:, 'rdb2RdfConfig', ?config_id)
    ?st = plainLiteral(?status)
  From
    "rdb2rdfexecution"






Create View rdb2rdfExecution As
  Construct {
    ?s
      o:logEntries ?o ;
      .
  }
  With
    ?s = uri(r:, 'rdb2RdfExecution', ?id)
    ?o = uri(r:, 'rdb2RdfExecutionLogEntry', ?id)
  From
    "rdb2rdfexecution"


Create View rdb2rdfExecution_logmessages As
  Construct {
    ?s
      //rdfs:label ?l
      o:logEntry ?e ;
      .
  }
  With
    ?s = uri(r:, 'rdb2RdfExecutionLogEntry', ?rdb2rdfexecution_id)
//    ?e = uri(r:, 'rdb2RdfExecution', ?rdb2rdfexecution_id)
    ?e = plainLiteral(?text)
//    ?lvl = plainLiteral(?level)    
  From
    "rdb2rdfexecution_logmessages"

  