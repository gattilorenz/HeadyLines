<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="news.*"%>
<%@page import="main.*"%>
<%@page import="java.util.*"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Heady-Lines</title>
<link rel="icon" 
      type="image/png" 
      href="img/favicon.png">
<!-- jQuery live versions -->
<!-- <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<script src="//code.jquery.com/jquery-1.12.0.min.js"></script> 
<script
	src="//code.jquery.com/ui/1.11.4/jquery-ui.min.js"
	integrity="sha256-xNjb53/rY+WmG+4L6tTl9m6PpqknWZvRt0rO1SRnJzw="
	crossorigin="anonymous"></script> -->  

<!-- cached, offline versions -->
<link rel="stylesheet" href="css/jquery-ui.css">
<script src="js/jquery-1.12.0.min.js"></script>
<script src="js/jquery-ui.js"></script>

<link rel="stylesheet" href="css/timeline.css">
<script src="js/tsort.js"></script>
<link rel="stylesheet" href="css/tablestyle.css" type="text/css" />
<script> //post the modified sentence to the final page
function doPost(url, headline) {
    var jForm = $('<form></form>');
    jForm.attr("style","display:none;");
    jForm.attr('action', url);
    jForm.attr('method', 'post');
    var jInput = $("<input>");
    jInput.attr('name', 'finalHeadline');
    jInput.attr('value', headline);
    jForm.append(jInput);
    jForm.appendTo('body').submit();
    //jForm.submit();
}

$(function(){
    $("a[post=true]").each(function () {
        $(this).on('click', function () {
            doPost(
                $(this).attr('phref'),
                $(this).attr('pdata')
            );
        });
    });
});

      </script>
</head>
<body>
	<div id="header">
		<div id="headertitle">
			<span><a href="index.jsp"><img style="height: 65px"	src="img/logo.png"></a></span>
			<span style="margin-left: 30px; font-size: 33px;">Headline selection</span>
		</div>
	</div>
	<div id="main">
		<%
		NewsObject selectedNewsObject = (NewsObject) session.getAttribute("selectedNewsObject");
		News selectedNews = (News) session.getAttribute("selectedNews");
		Quote allQuotes[] = (Quote[]) session.getAttribute("sortedQuotes");
		//get all the ids of the quotes that are selected
		String jsonQuoteIDArray = request.getParameter("activeQuotes");
		ArrayList<Integer> quotesToTake = new ArrayList<>(); 
		if (jsonQuoteIDArray != null && jsonQuoteIDArray.length() > 3) {
			//poor man's JSON Array parsing
			String[] quoteIDS = jsonQuoteIDArray.substring(2,jsonQuoteIDArray.length()-2).split(",");
			for (String id : quoteIDS) {
				id = id.replace('"', ' ');
				if (!id.trim().equals("") )
					quotesToTake.add(Integer.parseInt(id.trim()));
			}
		}
		Quote similarQuotes[] = new Quote[quotesToTake.size()];
		int i = 0;
		//System.out.println("I got these indices:" + jsonQuoteIDArray);
		for (Integer qIdx : quotesToTake) {
			//System.out.println("taking" + allQuotes[qIdx].quote);
			similarQuotes[i] = allQuotes[qIdx];
			i++;
		}
		String errorString = "";
		if (similarQuotes.length == 0) {
			//TODO: we have no similarity sentences, show a message
			errorString = "Either no quote was good enough for the system, or you disabled them all.";
		}
		HeadyLines headyLines = (HeadyLines) session.getAttribute("headyLines");
		
		HashMap<String, Float> modifiedSentences = null;
		if (similarQuotes.length > 0) {
			modifiedSentences = headyLines.createHeadlines(selectedNews,similarQuotes);
			if (modifiedSentences==null || modifiedSentences.keySet().size()==0) {
				errorString = "I cannot find a possible modification for the sentences that were selected.";
			}
		}
		%>
		<!-- news box -->
		<div id="selectednewsdesc">
			<%=selectedNews.description%>
		</div>
		<% if (!errorString.equals("")) {
			%>
			<h3 style="text-align: center;"><%= errorString %></h3>
			<h4 style="text-align: center;">You can try to go back and add more sentences, or change the ingredients</h4>
			</div>
			</body>
			</html>
			<%
			return;
		}
		%>
		<div id="quotes">
			<table class="tableSorter"	style="width: 100%">
			<tr><th id="tableHeader">Sort quotes</th> </tr>
				<%
				for (Map.Entry<String,Float> entry : modifiedSentences.entrySet()) {
				%>
				<tr> 
 					<td class="choosableTitle"
						data-sortAs="<%=(int) (entry.getValue() * 1000)%>">
						<a post="true" phref="finalPage.jsp"
						pdata='<%=entry.getKey()%>'><%=entry.getKey().trim().substring(0, 1).toUpperCase() + entry.getKey().trim().substring(1)%></a>
					</td>
				</tr>
				<% 
					}
				%>
			</table>
		</div>

	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('table.tableSorter').tableSort(
			 	{ speed: 2500}		
			);

			$( "#tableHeader" ).hide();
			setTimeout(function() {
				$( "#tableHeader" ).trigger( "click" );
			}, 500);
		});
	</script>
</body>
</html>