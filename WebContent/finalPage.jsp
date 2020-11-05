<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="news.*"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.regex.Pattern"%>


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

<style>
#header img {
	cursor: pointer;
}
</style>

</head>
<body>
	<div id="header">
		<div id="headertitle">
			<span><a href="index.jsp"><img style="height: 65px"	src="img/logo.png"></a></span>
			<span style="margin-left: 30px; font-size: 33px;">Headline in context</span>
		</div>
	</div>
	<div id="main">
		<%
			String finalTitle = request.getParameter("finalHeadline");
			if (finalTitle != null)
				finalTitle = finalTitle.trim().substring(0, 1).toUpperCase() + finalTitle.trim().substring(1);
			NewsObject selectedNewsObject = (NewsObject) session.getAttribute("selectedNewsObject");
			String bookmarkThis = request.getParameter("bookmarkThis");
			if (bookmarkThis != null && bookmarkThis.equals("yes")) {
		   		NewsRetriever newsRetriever = (NewsRetriever) session.getAttribute("newsRetriever");
		   		if (newsRetriever == null)
		   			newsRetriever = new NewsRetriever();
		   		newsRetriever.bookmarkNews(selectedNewsObject);
			}
				
		%>
		<!-- news box -->
		<div id="news">
			<div id="article">
			<!--  <img id="newspaperimage" src="img/newspaper.png">-->
 				<div id="headline">
					<h1 style="text-align: center"><%=finalTitle%></h1>
				</div>
				<div id="description">
					<a post="true" phref="finalPage.jsp" pdata='<%=finalTitle%>'><h2 style="text-align: center"><%=selectedNewsObject.description%></h2></a>
				</div>
				<div id="news_content"> 
					<p>Bill Keller Facebook hyperhyperhyperlocal curmudgeon Bill
						Keller Foursquare community Aron Pilhofer audience atomization
						overcome RT media diet AP, column-inch learnings Robin Sloan a
						giant stack of newspapers that you'll never read kitchen table of
						the future content is king Julian Assange Rupert Murdoch CNN
						leaves it there Arab spring. The Weekender media diet community
						Gutenberg parenthesis CTR the medium is the message content is
						king Gutenberg should isn't a business model WordPress Arianna,
						CPC Zite engagement afternoon paper blog trolls tags Gawker
						inverted pyramid.</p>
					<p>I love the Weather and Opera section David Foster Wallace
						tools trolls CTR curation just across the wire net neutrality
						anonymity HuffPo fourth estate, gotta grok it before you rock it
						fair use a giant stack of newspapers that you'll never read iPhone
						app experiment afternoon paper Robin Sloan hyperhyperhyperlocal.
						in the slot tweet Snarkmarket view from nowhere he said she said
						masthead digital first got drudged Lucius Nieman, attracting young
						readers trolls community Gutenberg parenthesis paidContent
						reporting Mozilla WordPress +1, go viral content is king iPad app
						Dayton for under $900 a day going forward totally blowing up on
						Twitter content farm.</p>

					<p>Article Skimmer SEO bringing a tote bag to a knife fight
						shoot a video ProPublica go viral David Foster Wallace, Knight
						Foundation The Weekender get me rewrite WikiLeaks Jeff Jarvis hot
						news doctrine put the paper to bed, copyboy layoffs Jeff Jarvis
						copyboy Groupon Neil Postman. inverted pyramid reporting backpack
						journalist NYT RD Like button crowdfunding he said she said, The
						Work of Art in the Age of Mechanical Reproduction Aron Pilhofer
						paidContent Gardening and War section syndicated tweets A.J.
						Liebling, he said she said nut graf The Work of Art in the Age of
						Mechanical Reproduction Colbert bump do what you do best and link
						to the rest. meme Project Thunderdome Encyclo Pictures of Goats
						section try PR Bill Keller perfect for starting a campfire
						cognitive surplus Innovator's Dilemma net neutrality lede,
						right-sizing Aron Pilhofer cognitive surplus dingbat Project</p>
				</div>
			</div>
		</div>
	</div>
	
	  <script>
	  function doPost(url, headline) {
		    var jForm = $('<form></form>');
		    jForm.attr("style","display:none;");
		    jForm.attr('action', url);
		    jForm.attr('method', 'post');
		    var jInput = $("<input>");
		    jInput.attr('name', 'finalHeadline');
		    jInput.attr('value', headline);
		    jForm.append(jInput);
		    var jInput2 = $("<input>");
		    jInput2.attr('name', 'bookmarkThis');
		    jInput2.attr('value', 'yes');
		    jForm.append(jInput2);
		    jForm.appendTo('body').submit();
		}

		$(function(){
		    $("a[post=true]").each(function () {
		        $(this).on('click', function () {
		        	<%
		        	//if we have bookmarked the news already, don't show the option
		        	if (bookmarkThis != null && bookmarkThis.equals("yes")) {
					%>
						event.preventDefault();
						return;
		        	<%} else {%>
		            doPost(
		                $(this).attr('phref'),
		                $(this).attr('pdata')
		            );
		            <% }%>
		        });
		    });
		});
		//and also d not show the tooltip
		<%if (bookmarkThis == null || !bookmarkThis.equals("yes")) {%>
		  $(function() {
			    $( document ).tooltip({
			      items: "h2",
			      track: true,
			      content: function() {
			        var element = $( this );
			        return "<div style='margin:5px'><img src='img/heart-add-256x256.png' style='height: auto; width: auto; max-width: 35px; vertical-align:middle';> Bookmark this article</div>";
			      }
			    });
			  });
		<%}%>
	  </script>
	
</body>
</html>