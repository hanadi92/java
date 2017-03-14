<strong>How to use Megal triple store jar</strong>
<ul>
<li>Place the Mega Models directory one level higher than the ModelRDF directory</li>
<li>Start command line inside the ModelRDF folder (Where the ModelRDF.jar is located)</li>
<li>Create a file of the intended SELECT or UPDATE SPARQL you want to execute on the MegalTripleStore</li>
<li>Enter (Java -jar "ModelRDF.jar" name-of-query-file type-of-query) where type-of-query is either (select) or (update)</li>
<li>A (Megals.ttl) file will be created which holds in the triple store of the used mega models (Models directory), if this file already exists then it will be used for queries. Therefore, delete the Megals.ttl file if a newer version of the (Models) directory is created and the (Megalts.ttl) will be updated.</li>
<li>Behaviour of SPARQL query results:
<ul>
<li>If the used query is select then the solution will be printed in the command line as well as stored in (selectResults) file</li>
<li>If the used query is update then the updated MegalTripleStore will be stored in a new file in the same working directory level called (updated_"name-of-query-file".ttl)</li>
</ul>
</li>
</ul>
