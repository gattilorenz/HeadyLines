<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="news.*"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.time.LocalDate"%>
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
<link rel="stylesheet" href="css/newsstroll.css">
<script src="js/stroll.js"></script>

<title>Headline Generator - Find concepts</title>
</head>
<body>
	<div id="header">
		<div id="headertitle">
			<span><a href="index.jsp"><img style="height: 65px"	src="img/logo.png"></a></span>
			<span style="margin-left: 30px; font-size: 33px;">Concept extraction</span>
		</div>
	</div>
	<div id="main">
		<%
			NewsObject selectedNews = null;
			NewsRetriever retriever = (NewsRetriever) session.getAttribute("newsRetriever");
			//if the user gave me a news element
			if (request.getParameter("typedNewsDesc") != null) {
				String desc = request.getParameter("typedNewsDesc");
				selectedNews = new NewsObject("title",desc,"user",LocalDate.now());
			}
			else {
				int index = 0;
				try {
					index = Integer.parseInt(request.getParameter("selectedNewsIndex"));
					ArrayList<NewsObject> newsArray = (ArrayList<NewsObject>) session.getAttribute("newsArray");
					selectedNews = newsArray.get(index);
				} catch (Exception e) {

				}
				

			}
			session.setAttribute("selectedNewsObject", selectedNews);
			HashMap<String, String> ingredients = retriever.getIngredients(selectedNews);
			//session.removeAttribute("newsRetriever");
			//session.removeAttribute("selectedNewsIndex");
		%>
		<!-- news box -->
		<div id="news">
			<div id="headlinestopwords">
				<div class="hiddenText" style="display: none;"><%=selectedNews.description%></div>
				<div class="headlineConcepts">
					<%
						ArrayList<String> tokens = retriever.findConceptsInHeadline(selectedNews);

						//foreach token, put text in a span with a field is_stopword if stopword
						//then strikethrough one word at time
						for (int i = 0; i < tokens.size(); i++) {
							String token = tokens.get(i);
							String tokenType = "";
							String[] tokenFields = token.split("#");
							token = tokenFields[0];
							tokenType = tokenFields[tokenFields.length - 1];
							String lemmaPos = "";
							String onClick = "";
							if (tokenFields.length > 2) {
								lemmaPos = tokenFields[1] + "#" + tokenFields[2];
								onClick = "changeLemmaState('"+lemmaPos+"')";
							}
							
							
							//System.out.println("token: "+token+" matches ^[a-zA-Z0-9`]+.*?" + token.matches("^[a-zA-Z0-9`]+.*"));

							if (!token.matches("^[a-zA-Z0-9`]+.*")) {
					%><span
						class="<%=tokenType%>" id="<%=lemmaPos%>" onClick="<%=onClick%>">TRIMNEWLINE <%=token%></span>
					<%
						} else {
					%>
					<span class="<%=tokenType%>" id="<%=lemmaPos%>" onClick="<%=onClick%>"><%=token%></span>
					<%
						}
						}
					%>
				</div>
				<div class="button">
					<a post='true' phref='findSimilarity.jsp' style="text-decoration: none; color: inherit;">
						<div class="button">Find expressions</div>
					</a>
				
			</div>
			<div id="ingredients">
				<ul class="grow">
					<%
						int row = -1;
						int column = 0;
						String color;
						for (HashMap.Entry<String, String> entry : ingredients.entrySet()) {
							//colonna pari
							if (column == 0) {
								if ((row & 1) == 0)
									//riga pari
									color = "#fff";
								else
									color = "#eee";
					%>
					<li style="overflow: hidden; margin: 0px; padding: 0px;">
						<div class="left" onClick="changeIngredientState('<%=entry.getKey()%>');"
							style="padding-top: 10px; padding-bottom: 10px; border-right: thick solid #000000; border-width: 1px; float: left; width: 50%; height: 100%; text-align: center; background: <%=color%>"><%=entry.getKey().replaceAll("#[nvar]","")%>
							<i>(<%=entry.getValue()%>)
							</i>
						</div> <%
 	column++;
 			//colonna dispari
 		} else {

 			if ((row & 1) == 0)
 				//riga pari
 				color = "#eee";
 			else
 				color = "#fff";
 %>
						<div class="right" onClick="changeIngredientState('<%=entry.getKey()%>');"
							style="padding-top: 10px;  padding-bottom: 10px;  margin: 0px; height: 100%; width: auto; text-align:center; background: <%=color%>"><%=entry.getKey().replaceAll("#[nvar]","")%>
							<i>(<%=entry.getValue()%>)
							</i>
						</div>
					</li>
					<%
						column = 0;
								row++;
							}
						}
					%>
				</ul>
			</div>
		</div>

	</div>
	<script>
		window.ingredientsRemovedArray = [];
		window.lemmasAddedArray = [];
		var $ingredientsDiv = $("#ingredients");
		var $buttonNext = $(".button");
		$ingredientsDiv.hide();
		$buttonNext.hide();
		var $el = $(".hiddenText"), text = $.trim($el.text()), words = text
				.split(" ");

		var $div = $(".headlineConcepts");
		//togliamo spazio dopo parentesi aperta (cioè un newline) ma assicuriamoci che ci sia prima
		$div.html($div.html().replace(/-LRB-<\/span>(\n\s+)*/g, ' (</span>'));
		//togliamo spazio prima di parentesi chiusa
		$div.html($div.html().replace(/(\n\s+)*(<span.*?>)-RRB-/g, '$2)'));
		//togliamo spazio prima di TRIMNEWLINE, se c'è
		$div.html($div.html().replace(/(\n\s+)*(<span.*?>)TRIMNEWLINE /g,
						'$2'));
		//verifichiamo che non rimangano RRB o LRB
		$div.html($div.html().replace(/-LRB-/g, '('));
		$div.html($div.html().replace(/-RRB-/g, ')'));
		$div.html($div.html().replace(/`<\/span>\s*/, '`</span>'));
		$div.html($div.html().replace('``', '"'));
		
		//var count = 1;

		setTimeout(function() { //wait 1.5 seconds before doing anything

			$div.children().each(function(i) {
				if ($(this).hasClass("CONTENT")) {
					$(this).delay(250 * i).animate({
						color : "rgb(119, 175, 255)" //light blue
					}, 1500);
					//count++;
				} else if ($(this).hasClass("ENTITY")) {
					$(this).delay(250 * i).animate({
						color : "rgb(175, 230, 119)" //green
					}, 1500);
				} else if ($(this).hasClass("UNKNOWNCONTENT")) {
					$(this).delay(250 * i).animate({
						color : "rgb(255, 119, 119)" //red
					}, 1500);
				} else {
					$(this).delay(250 * i).animate({
						color : "rgb(153, 153, 153);" //grayed out
					}, 1500);
				}
			});
		}, 400);

		setTimeout(function() {
			$ingredientsDiv.fadeIn("slow");
			$buttonNext.fadeIn();
		}, 350 * $div.children().size());

		setTimeout(function() {
			$buttonNext.fadeIn("slow");
		}, 350 * $div.children().size() + 500);
		
		
		//to get cell and disable ingredient
		function changeIngredientState(ingredient){
			var ingredientIsAlreadyInArray = false;
			var firstEmptyPos = -1;

			for (var i=0; i < window.ingredientsRemovedArray.length; i++) {
				if (window.ingredientsRemovedArray[i] === "")
					firstEmptyPos = i;
				if (window.ingredientsRemovedArray[i] === ingredient) {
					ingredientIsAlreadyInArray = true;
					window.ingredientsRemovedArray[i] = "";
					var elements = $('.left, .right').each(function () {
						if ($(this).attr("onClick").indexOf(ingredient) >= 0) 
							$(this).css("color", "");
					})
				}
			}
			if (!ingredientIsAlreadyInArray) {
				var pos = window.ingredientsRemovedArray.length;
				if (firstEmptyPos >= 0) {
					pos = firstEmptyPos;
				}
				window.ingredientsRemovedArray[pos] = ingredient;
				var elements = $('.left, .right').each(function () {
					if ($(this).attr("onClick").indexOf(ingredient) >= 0) 
						$(this).css("color", "#e0e0e0;");
				})
			}
		};

		//enable and disable stopwords in the title
		function changeLemmaState(lemma){
			var lemmaIsAlreadyInArray = false;
			var firstEmptyPos = -1;

			for (var i=0; i < window.lemmasAddedArray.length; i++) {
				if (window.lemmasAddedArray[i] === "")
					firstEmptyPos = i; //since we are looping, save the first empty pos
				if (window.lemmasAddedArray[i] === lemma) { //if we did add the lemma before
					lemmaIsAlreadyInArray = true; //the lemma is already there
					window.lemmasAddedArray[i] = ""; //so remove it
					//and then change it back to the original color
					var elements = $('.headlineConcepts > span').each(function () {
						if ($(this).attr("onClick").indexOf(lemma) >= 0)
							if ($(this).hasClass("CONTENT")) {
								$(this).css("color", "rgb(119, 175, 255)"); //light blue
							} else if ($(this).hasClass("ENTITY")) {
								$(this).css("color", "rgb(175, 230, 119)"); //green
							} else if ($(this).hasClass("UNKNOWNCONTENT")) {
								$(this).css("color", "rgb(255, 119, 119)"); //red
							} else {
								$(this).css("color", "rgb(153, 153, 153);"); //grayed out
							}
					})
				}
			}
			if (!lemmaIsAlreadyInArray) {
				//the lemma is not in the array, se we have two options
				//either the lemma is gray, then we colour it grey
				//or it is black, and then we add a blue colour to it
				var pos = window.lemmasAddedArray.length;
				if (firstEmptyPos >= 0) {
					pos = firstEmptyPos;
				}
				//remove derived ingredients
				changeDerivedFrom(lemma);
				window.lemmasAddedArray[pos] = lemma;
				var elements = $('.headlineConcepts > span').each(function () {
					if ($(this).attr("onClick").indexOf("'" + lemma + "'") >= 0) {
						if ($(this).hasClass("STOP")) {
							$(this).css("color", "rgb(119, 175, 255)"); //light blue
						}
						else {
							$(this).css("color", "rgb(153, 153, 153);"); //grayed out
						}
				}
				});
			}
		}
		
		function changeDerivedFrom(lemmaPos){
			var ingredientIsAlreadyInArray = false;
			var firstEmptyPos = -1;
			var derivedItems = [];
			//get all the derived items
			var originalPos = "";
			if (lemmaPos.match(/#a$/))
				originalPos = "adjective";
			else if  (lemmaPos.match(/#n$/))
				originalPos = "noun";
			else if  (lemmaPos.match(/#v$/))
				originalPos = "verb";
			else if  (lemmaPos.match(/#r$/))
				originalPos = "adverb";
			var lemma = lemmaPos.substring(0,lemmaPos.length-3);
			var elements = $('.left, .right').each(function () {
				if (($(this).html().match(originalPos+"\s*</i>") >= 0)
						&&
						($(this).html().indexOf(lemma) >= 0)) {
					var derivedLemmaPos = $(this).attr("onclick");
					derivedLemmaPos = derivedLemmaPos.substring(23,derivedLemmaPos.length-3);
					derivedItems[derivedItems.length] = derivedLemmaPos;
				}
			});
			for (var i=0; i < window.ingredientsRemovedArray.length; i++) {
				for (var j=0; j < derivedItems.length; j++) {
					if (window.ingredientsRemovedArray[i] === derivedItems[j]) {
						derivedItems[j] = "";
					}
				}
			}
			for (var j=0; j < derivedItems.length; j++) {
				if (derivedItems[j] === "") {
					continue;
				}
				changeIngredientState(derivedItems[j]);
			}
			
		};
		
		//post the array to the next page
		$("a[post=true]").each(function () {
			$(this).on('click', function () {
					doPost($(this).attr('phref'),JSON.stringify(window.ingredientsRemovedArray),JSON.stringify(window.lemmasAddedArray));
             });
		});
		
		function doPost(url, ingredientsString, lemmasString) {
    		    var jForm = $('<form></form>');
    		    jForm.attr("style","display:none;");
    		    jForm.attr('action', url);
    		    jForm.attr('method', 'post');
    		    var jInput1 = $("<input>");
    		    jInput1.attr('name', 'ingredientsSelectedArray');
    		    jInput1.attr('value', ingredientsString);
    		    var jInput2 = $("<input>");
				jInput2.attr('name', 'lemmasAddedArray');
	    		jInput2.attr('value', lemmasString);
    		    jForm.append(jInput1);
    		    jForm.append(jInput2);
    		    jForm.appendTo('body').submit();
    	};
	</script>

	<script>
		//Bind via selectors
		stroll.bind('ul');
	</script>

</body>
</html>