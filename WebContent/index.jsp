<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="news.*"%>
<%@page import="main.*"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.List"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.util.Date"%>
 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Heady-Lines</title>
<link rel="icon" 
      type="image/png" 
      href="img/favicon.png">

<script src="js/jquery-1.12.0.min.js"></script>
<script src="js/jquery-ui.js"></script>
<script src="js/jquery.isloading.min.js"></script>

<link rel="stylesheet" href="css/jquery-ui.css">
<link rel="stylesheet" href="css/font-awesome.css" >

<link rel="stylesheet" href="css/timeline.css">
<link rel="stylesheet" href="css/newsstroll.css">
<script src="js/stroll.js"></script>
</head>

<body>
	<div id="header">
		<div id="headertitle">
			<span><img style="height: 65px"	src="img/logo.png"></span>
			<span style="margin-left: 30px; font-size: 33px;">Event	selection</span>
		</div>
	</div>
	<div id="main"> 

		<div id="controls" style="display:flex;justify-content:center;align-items:center;">
			<!-- slider box -->
			<div id="slider-box" style="text-align: center; float:left;">
				<div style="margin-left: 50px;">
					<div id="slider-label" style="margin-bottom: 12px">Positivity threshold:</div>
					<div id="slider-range" style="text-align: center; margin: auto;"></div>
				</div>
			</div>
			<div id="bookmark-box" style="text-align:center">
				<div id="bookmarks">
					<div style="margin-bottom: 6px;">Bookmarked articles</div>
					<div>
						<img src="img/heart.png"
							style='height: auto; width: auto; max-width: 25px; vertical-align: middle'>
					</div>
				</div>
			</div>	
			<div id="calendar-box"
				style="margin-bottom: 12px; margin-top: -3%; float: right">
				<div id="calendar-label"
					style="margin-bottom: 7px; text-align: center; width: 100%">Date</div>
				<div>
					<input type="text" id="datepicker" size="30"
						style="margin: auto; width: 80%; text-align: center;">
				</div>
			</div>
		</div>
		<br />
		<!-- news box -->
		<div id="news">
			<div id="article">
				<ul class="grow" style="max-height: 600px;">

				</ul>
			</div>
		</div>
	</div>
	
	<div style="text-align:center"><textarea></textarea></div>
	
<%
	//all lowercase, please!
	List<String> forbiddenTopics = Arrays.asList(new String[]{"terror", "dead", "death", "body", "corpse",
	"isis", "islamic", "rape", "kill", "war", "wound", "stab", "injur", "bomb"});
	session.setMaxInactiveInterval(-1); //session will not expire until you close the browser
	
	HeadyLines headyLines = (HeadyLines) session.getAttribute("headyLines");
	if (headyLines == null)
		headyLines = new HeadyLines();
	session.setAttribute("headyLines", headyLines);
	NewsRetriever newsRetriever = (NewsRetriever) session.getAttribute("newsRetriever");
	if (newsRetriever == null)
		newsRetriever = new NewsRetriever();
	String requestedDate = request.getParameter("selectedDate");
	System.out.println("SERVER STARTED!");
	
	ArrayList<NewsObject> newsArray = (ArrayList<NewsObject>) session.getAttribute("newsArray");
	
	if (requestedDate != null && !requestedDate.equals("")) {
		if (newsArray != null) {
			newsArray.clear();
		}
		try {
			if (requestedDate.equals("bookmark")) {
				newsArray = newsRetriever.retrieveBookmarkedNews();
			} else {
				DateTimeFormatter js_DTF = DateTimeFormatter.ofPattern("MM/dd/yyyy");
				System.out.println("Retrieving news for " + requestedDate);
				newsArray = newsRetriever.retrieveNews(LocalDate.parse(requestedDate,js_DTF));
			}
		} catch (Exception e) {
			//if something bad happens, just put the news we had before	
			newsArray = (ArrayList<NewsObject>) session.getAttribute("newsArray");
		}
	};
	//if array is still null or empty, get today's news
	if (newsArray == null || newsArray.size() < 1) {
		System.out.println("Retrieving today's news");
		newsArray = newsRetriever.retrieveNews(LocalDate.now());
		requestedDate = "";
	}
	System.out.println("news retrieved");
	session.setAttribute("newsRetriever", newsRetriever);
	session.setAttribute("newsArray", newsArray);
%>
<script>
		var $requestedDate = "<%=requestedDate%>";
		//we put all the news in a javascript array so the filtering works ok
		var allNews = [
							<%String jsArray = "";
			for (int i = 0; i < newsArray.size(); i++) {
				NewsObject news = newsArray.get(i);
				for (String forbiddenWord : forbiddenTopics) {
					if (news.description.contains(forbiddenWord)) {
						news.sentiment = 0;
					}
				}
				String liObj = "\"<li valence='" + news.sentiment + "'><a post='true' phref='extractConcepts.jsp'"
						+ "pdata='" + i + "'> <img src='img/" + news.source.toLowerCase() + ".png'> <span>"
						+ news.description.replace("\"", "\\\"") + "</span>" + "</a></li>\"";
				jsArray = jsArray + ",\n" + liObj;

			}%>
							<%=jsArray%>
		                   ]
		var displayedNews = allNews.join("");
		
         $(function() {
        	$( ".grow" ).html(displayedNews);
        	 
           $( "#slider-range" ).slider({
             range: true,
             min: 0,
             max: 4,
             values: [ 0, 4 ],
             slide: function( event, ui ) {
            	 var actual_min_value = ui.values[ 0 ];
            	 var actual_max_value = ui.values[ 1 ];
            	 var tabella = "";
            	 for (var tmpIdx = 0; tmpIdx < allNews.length; tmpIdx++) {
            		 var currentLi = $.parseHTML( allNews[tmpIdx] );
            		 var li_valence = $( currentLi ).attr("valence");
            		 if (li_valence >= actual_min_value && li_valence <= actual_max_value) {
            			 tabella = tabella + allNews[tmpIdx];
            		 }
            	 }

            	 $( ".grow" ).html(tabella);
                 $("a[post=true]").each(function () {
                     $(this).on('click', function () {
                         doPost(
                             $(this).attr('phref'),
                             "selectedNewsIndex",
                             $(this).attr('pdata')
                         );
                     });
                 });

             }
           });
           
           $("a[post=true]").each(function () {
               $(this).on('click', function () {
                   doPost(
                       $(this).attr('phref'),
                       "selectedNewsIndex",
                       $(this).attr('pdata')
                   );
               });
           });
           
         });
         
	stroll.bind( '#news ul', { live: true } );

	if ($requestedDate === "bookmark") {
		$("textarea").css("display","block");
	}
	else {
		$("textarea").css("display","none");
	}
	
	 $( "#datepicker" )
	
	//UNCOMMENT THIS IF THERE IS NO INTERNET ACCESS
	//this way you can only pick from dates that are in the DB
<%-- 	<% 
	    String enabledDates = "";
		ArrayList<String> dates = newsRetriever.getDatesInDB();
		for (String date : dates) {
			enabledDates = enabledDates + ", \"" + date + "\"";
		}
		enabledDates = enabledDates.replaceFirst(",", "");
	%>
	var enabledDatesArray = [ <%= enabledDates%> ];

	function DisableSpecificDates(date) {
	    var string = jQuery.datepicker.formatDate('dd-mm-yy', date);
	    return [enabledDatesArray.indexOf(string) > -1]; //enable only dates that are not in the array
	  } --%>
	
	  function doPost(url, parName, parValue) {
		    var jForm = $('<form></form>');
		    jForm.attr("style","display:none;");
		    jForm.attr('action', url);
		    jForm.attr('method', 'post');
		    var jInput = $("<input>");
		    jInput.attr('name', parName);
		    jInput.attr('value', parValue);
		    jForm.append(jInput);
		    jForm.appendTo('body').submit();
		    //jForm.submit();
		}
 
	  function showLoadingIndicator(message) {
	 		$("#article").isLoading({
			      text:       message,
    			  position:   "overlay",
    			  'tpl': '<span class="isloading-wrapper %wrapper%">%text%<i class="fa fa-refresh fa-spin"></i></span>',
    			  disableOthers: [
						$( "#datepicker" ), //disable choosing another date
						$( "#slider-range"), //disable moving the slider (breaks loading overlay)
						$( "ui-slider-handle"),
						$( "textarea ")
							
    			  ]
		  });
	  }
	  
	  $( "#datepicker" ).datepicker({
	   	  showAnim: "slideDown",
	   	  showButtonPanel: true,
	   	  currentText: "Today", 
	   	  minDate: new Date(2015, 10, 29),
	   	  maxDate: 0,
	   	  defaultDate: +0,
	   	  constrainInput: true,
	   	  //beforeShowDay: DisableSpecificDates,
	   	  onSelect: function(date) {
	   			showLoadingIndicator("Downloading and analyzing news");
	   			doPost("index.jsp","selectedDate",date);
	      }
	    });	    
	
	$('#bookmark-box').click(function() {
		showLoadingIndicator("Retrieving and analyzing saved news");
		doPost("index.jsp", "selectedDate", "bookmark");
	})
	
	
	$("textarea").keydown(function(event) {
	    if (event.keyCode == 13) {
	 		var $desc = $("textarea").val(); 
	    	$("textarea").val("");
	    	doPost("extractConcepts.jsp","typedNewsDesc",$desc);
	   		//TODO: complete this
	    }
	});
	</script>
</body>
</html>