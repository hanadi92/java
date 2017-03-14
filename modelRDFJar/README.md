<strong>How to use Megal triple store jar</strong>
<ul>
<li>Locate the Mega Models directory one level higher than the ModelRDF directory</li>
<li>Start command line inside the ModelRDF folder (Where the ModelRDF.jar is located)</li>
<li>Create a file of the intended SELECT or UPDATE SPARQL you want to execute on the MegalTripleStore</li>
<li>Enter (Java -jar "ModelRDF.jar" name-of-query-file type-of-query) where type-of-query is either (select) or (update)</li>
<li>A megals.ttl file will be created which holds in the triple store of the used mega models</li>
<li>If the used query is select then the solution will be printed in the command line</li>
<li>If the used query is update then the updated MegalTripleStore will be added into a new file in the same working directory level</li>
</ul>