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
<script src="js/isortope.js"></script>

<link rel="stylesheet" href="css/timeline.css">
<link rel="stylesheet" href="css/newsstroll.css">

<link rel="stylesheet" href="css/tablestyle.css" type="text/css" />
</head>
<body>
	<div id="header">
		<div id="headertitle">
			<span><a href="index.jsp"><img style="height: 65px"	src="img/logo.png"></a></span>
			<span style="margin-left: 30px; font-size: 33px;">Well-known expressions ranking</span>
		</div>
	</div>
	<div id="main">
		<%
			NewsObject selectedNews = (NewsObject) session.getAttribute("selectedNewsObject");
			if (selectedNews == null)
				System.out.println("news it's null, crap!");
			HeadyLines headyLines = (HeadyLines) session.getAttribute("headyLines");
			if (headyLines == null)
				System.out.println("headyLines it's null, crap!");

			Vector<Ingredient> ingredients = selectedNews.getIngredients();
			//System.out.println("got " + ingredients.size() + " ingredients");
			ArrayList<NewsIngredient> tmpIngrArrayList = new ArrayList<>(ingredients.size()); 
			//

			int i = 0;
			//get the ingredients that the user selected
			String jsonIngredientsArray = request.getParameter("ingredientsSelectedArray");
			HashMap<String,Integer> ingredientsSelectedByUser = new HashMap<>(); 
			if (jsonIngredientsArray != null && jsonIngredientsArray.length() > 3) {
				//poor man's JSON Array parsing
				String[] ingredientsSelectedByUserArray = jsonIngredientsArray.substring(2,jsonIngredientsArray.length()-2).split("\",\"");
				for (String ing : ingredientsSelectedByUserArray) {
					ingredientsSelectedByUser.put(ing,1);
				}
			}
			
			//put also the lemmas among the ingredients used
			String jsonlemmasAddedArray = request.getParameter("lemmasAddedArray"); 
			if (jsonlemmasAddedArray != null && jsonlemmasAddedArray.length() > 3) {
				//poor man's JSON Array parsing
				String[] lemmasAddedByUserArray = jsonlemmasAddedArray.substring(2,jsonlemmasAddedArray.length()-2).split("\",\"");
				for (String ing : lemmasAddedByUserArray) {
					ingredientsSelectedByUser.put(ing,1);
				}
			}
			
			for (Ingredient ingredient : ingredients) {
				//skip ingredient if user selected it
				if (ingredientsSelectedByUser.containsKey(ingredient.getName()+"#"+ingredient.getPOS())) {
					//remove it, so we will be left only with "new" ingredients
					ingredientsSelectedByUser.remove(ingredient.getName()+"#"+ingredient.getPOS());
					continue;
				}
				//public NewsIngredient(String lemma, String wnPOS, String type, float probability) {
				//System.out.println(ingredient.getName() + " " + ingredient.getPOS() + " " + ingredient.getRelation());
				tmpIngrArrayList.add(new NewsIngredient(ingredient.getName(), ingredient.getPOS(), ingredient.getRelation(),
						ingredient.getLemmaProb()));
			}

			//add the ingredients that the user selected
			for (String ingredientSTR : ingredientsSelectedByUser.keySet()) {
				if (ingredientSTR.equals(""))
					continue;
				System.out.println(ingredientSTR);
				String[]  lemmaPos = ingredientSTR.split("#");
				NewsIngredient test = new NewsIngredient(lemmaPos[0],lemmaPos[1],"",0);
				tmpIngrArrayList.add(test);
			}

			//TODO: remove this, for demo only
			NewsIngredient removeMe = null;
			for (NewsIngredient ing : tmpIngrArrayList) {
				if (ing.lemma.startsWith("union"))
					removeMe = ing;
			}
			tmpIngrArrayList.remove(removeMe);
			
			NewsIngredient[] tmpIngr = new NewsIngredient[tmpIngrArrayList.size()];
			for (NewsIngredient ing : tmpIngrArrayList) {
				tmpIngr[i] = ing;
				i++;
			}

			News tmpNews = new News(selectedNews.headline, selectedNews.description, tmpIngr);
			Quote similarQuotes[] = headyLines.sortQuotesBySimilarity(tmpNews, headyLines.quotes, 2000);

			session.setAttribute("sortedQuotes",similarQuotes); //pass all the quotes, then get the ids to use via post using javascript
			session.setAttribute("selectedNews",tmpNews);

		%>
		<!-- news box -->
		<div id="selectednewsdesc">
			<%=selectedNews.description%>
		</div>
				<%
					if (similarQuotes == null || similarQuotes.length == 0) {
						%>
						<h3 style="text-align: center;">There are no famous expressions similar to this article.</h3>
						<h4 style="text-align: center;">You can try to go back and add more ingredients.</h4>
						</div>
						</body>
						</html>
						<%
						return;
					}
				%>
		<a post='true' phref="rankSubstitutions.jsp" style="text-decoration: none; color:inherit;"><div class="button">Generate headlines</div></a>
		<div id="quotes" style="display: table; width: 100%;">
		<form style="display: table-cell; float: left; margin-left: 2%;">		
		<fieldset>
			 <legend>Type</legend><br>
			 <input type="checkbox" class="type" id="song"  checked="checked"/> Songs
			 <br /> 
			 <input type="checkbox" class="type" id="movie"  checked="checked"/> Movies
			 <br />
			 <input type="checkbox" class="type" id="book"  checked="checked"/> Books
			 <br />
			 <input type="checkbox" class="type" id="saying"  checked="checked"/> Sayings
		</fieldset><br />
		<fieldset>
			 <legend>Years</legend><br>
			 <input type="checkbox" class="year" id="1996-2016"  checked="checked"/> 1996-2016
			 <br />
			 <input type="checkbox" class="year" id="1975-1995"  checked="checked"/> 1975-1995
			 <br /> 
			 <input type="checkbox" class="year" id="1950-1974"  checked="checked"/> 1950-1974 
			 <br />
			 <input type="checkbox" class="year" id="earlier"  checked="checked"/> earlier
		</fieldset>
		</form>

			<table class="tableSorter">
			<tr><th id="tableHeader" data-sort-type="col0" class="sortDesc">Sort index</th> <th data-sort-type="none">Quote</th></tr>
				<%
					for (i = 0; i < similarQuotes.length; i++) {
						String csscolor = "";
						
						if (similarQuotes[i].similarityWithNews < headyLines.quote_headlineSimThreshold) {
							csscolor = "rgb(153, 153, 153);";
						}
							
					
				%>
				<tr>
					<td><%=(int) (similarQuotes[i].similarityWithNews * 1000)%></td>
					<td>
						<span style="color: <%=csscolor%>" onClick="changeQuoteState(<%=i%>)" id="<%=i%>" class="quoteSpan"><%=similarQuotes[i].quote%></span>
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
			$(':checkbox').prop('checked', true);
			var $buttonNext = $(".button");
			$buttonNext.hide();
			$buttonNext.css("margin-bottom", "0px");
			$buttonNext.css("position", "absolute");
			$buttonNext.css("margin-left", "auto");
			$buttonNext.css("margin-right", "auto");
			$buttonNext.css("width", "250px");
			$buttonNext.css("right", "0");
			$buttonNext.css("left", "0");

			$( "#tableHeader" ).hide();
			//disable autoresort
			$('table.tableSorter').isortope({autoResort: false});
			$('table.tableSorter').css( "height", "" ); //don't know why, but probably tablesorter uses it
			//sort after we start seeing the page
			setTimeout(function() { 
				//$( "#tableHeader" ).trigger( "click" );
				$('table.tableSorter').isortope('resort');
				$('table.tableSorter').isortope('resort'); //to fix a bug with chrome?
			}, 600);
			setTimeout(function() {
				$buttonNext.fadeIn("slow");
			}, 4000);
			
			
		});
		
		window.quotesRemovedArray = [];
				
		var $quotedata = { "How to train your dragon" : {"type" : "movie", "year" : "1996-2016"} , "Baby one more time" : {"type" : "song", "year" : "1996-2016"} , "A hard day's night" : {"type" : "song", "year" : "1950-1974"} , "Son of a preacher man" : {"type" : "song", "year" : "1950-1974"} , "Losing my religion" : {"type" : "song", "year" : "1975-1995"} , "The catcher in the rye" : {"type" : "book", "year" : "1950-1974"} , "Through the looking glass" : {"type" : "book", "year" : "earlier"} , "What's my age again" : {"type" : "song", "year" : "1996-2016"} , "The hunt for Red October" : {"type" : "movie", "year" : "1975-1995"} , "Saving private Ryan" : {"type" : "movie", "year" : "1996-2016"} , "Total eclipse of the heart" : {"type" : "song", "year" : "1975-1995"} , "A whiter shade of pale" : {"type" : "song", "year" : "1950-1974"} , "Every breath you take" : {"type" : "song", "year" : "1975-1995"} , "It must have been love" : {"type" : "song", "year" : "1975-1995"} , "Stand by me" : {"type" : "song", "year" : "1950-1974"} , "Too much love will kill you" : {"type" : "song", "year" : "1975-1995"} , "The prince and the pauper" : {"type" : "book", "year" : "earlier"} , "Bridge over troubled water" : {"type" : "song", "year" : "1950-1974"} , "No woman no cry" : {"type" : "song", "year" : "1975-1995"} , "Who framed roger rabbit" : {"type" : "movie", "year" : "1975-1995"} , "Nothing compares to you" : {"type" : "song", "year" : "1975-1995"} , "The number of the beast" : {"type" : "song", "year" : "1975-1995"} , "The hunchback of Notre Dame" : {"type" : "book", "year" : "earlier"} , "What happens in Vegas" : {"type" : "saying", "year" : "earlier"} , "Rock around the clock" : {"type" : "song", "year" : "1950-1974"} , "Boulevard of broken dreams" : {"type" : "song", "year" : "1996-2016"} , "All you need is love" : {"type" : "song", "year" : "1950-1974"} , "I want it that way" : {"type" : "song", "year" : "1996-2016"} , "Sympathy for the devil" : {"type" : "song", "year" : "1950-1974"} , "I saw her standing there" : {"type" : "song", "year" : "1950-1974"} , "What to expect when you're expecting" : {"type" : "book", "year" : "1975-1995"} , "For whom the bell tolls" : {"type" : "book", "year" : "earlier"} , "Crime and punishment" : {"type" : "book", "year" : "earlier"} , "I want to break free" : {"type" : "song", "year" : "1975-1995"} , "Sweet dreams (are made of this)" : {"type" : "song", "year" : "1975-1995"} , "Lucy in the sky with diamonds" : {"type" : "song", "year" : "1950-1974"} , "(I can't get no) satisfaction" : {"type" : "song", "year" : "1950-1974"} , "Back to the future" : {"type" : "movie", "year" : "1975-1995"} , "Sgt. Pepper's lonely hearts club band" : {"type" : "song", "year" : "1950-1974"} , "One flew over the cuckoo's nest" : {"type" : "movie", "year" : "1975-1995"} , "Like a rolling stone" : {"type" : "song", "year" : "1950-1974"} , "Shine on you crazy diamond" : {"type" : "song", "year" : "1975-1995"} , "Can you feel the love tonight" : {"type" : "song", "year" : "1975-1995"} , "Three men in a boat" : {"type" : "book", "year" : "earlier"} , "Here comes the sun" : {"type" : "song", "year" : "1950-1974"} , "Love me tender" : {"type" : "song", "year" : "1950-1974"} , "I can't stop loving you" : {"type" : "song", "year" : "1950-1974"} , "The men who stare at goats" : {"type" : "book", "year" : "1996-2016"} , "Pride and prejudice" : {"type" : "book", "year" : "earlier"} , "Sweet child of mine" : {"type" : "song", "year" : "1975-1995"} , "Another one bites the dust" : {"type" : "song", "year" : "1950-1974"} , "The unexpected virtue of ignorance" : {"type" : "movie", "year" : "1996-2016"} , "I want to hold your hand" : {"type" : "song", "year" : "1950-1974"} , "Smells like teen spirit" : {"type" : "song", "year" : "1975-1995"} , "Eye of the tiger" : {"type" : "song", "year" : "1975-1995"} , "I can't buy me love" : {"type" : "song", "year" : "1950-1974"} , "Anarchy in the UK" : {"type" : "song", "year" : "1975-1995"} , "Crazy little thing called love" : {"type" : "song", "year" : "1975-1995"} , "Happiness is a warm gun" : {"type" : "song", "year" : "1950-1974"} , "The dark knight rises" : {"type" : "movie", "year" : "1996-2016"} , "There's something about Mary" : {"type" : "movie", "year" : "1996-2016"} , "With a little help from my friends" : {"type" : "song", "year" : "1950-1974"} , "My heart will go on" : {"type" : "song", "year" : "1996-2016"} , "The dark side of the moon" : {"type" : "song", "year" : "1950-1974"} , "How deep is your love" : {"type" : "song", "year" : "1975-1995"} , "You've lost that loving feeling" : {"type" : "song", "year" : "1950-1974"} , "Georgia on my mind" : {"type" : "song", "year" : "1950-1974"} , "Light my fire" : {"type" : "song", "year" : "1950-1974"} , "Strawberry fields forever" : {"type" : "song", "year" : "1950-1974"} , "Born on the fourth of July" : {"type" : "movie", "year" : "1975-1995"} , "We are never ever getting back together" : {"type" : "song", "year" : "1996-2016"} , "Wish you were here" : {"type" : "song", "year" : "1975-1995"} , "Magical mystery tour" : {"type" : "song", "year" : "1950-1974"} , "Money for nothing" : {"type" : "song", "year" : "1975-1995"} , "Another brick in the wall" : {"type" : "song", "year" : "1975-1995"} , "The wolf of Wall Street" : {"type" : "movie", "year" : "1996-2016"} , "The empire strikes back" : {"type" : "movie", "year" : "1975-1995"} , "God save the queen" : {"type" : "saying", "year" : "earlier"} , "When the saints go marching in" : {"type" : "song", "year" : "earlier"} , "In search of lost time" : {"type" : "book", "year" : "earlier"} , "Fifty shades of grey" : {"type" : "book", "year" : "1996-2016"} , "One hundred years of solitude" : {"type" : "book", "year" : "1950-1974"} , "When Harry met Sally" : {"type" : "movie", "year" : "1975-1995"} , "The long and winding road" : {"type" : "song", "year" : "1950-1974"} , "We can work it out" : {"type" : "song", "year" : "1950-1974"} , "Diamonds are forever" : {"type" : "saying", "year" : "earlier"} , "Wind of change" : {"type" : "song", "year" : "1975-1995"} , "A little less conversation" : {"type" : "song", "year" : "1950-1974"} , "Live and let die" : {"type" : "movie", "year" : "1950-1974"} , "I can't help falling in love" : {"type" : "song", "year" : "1950-1974"} , "The lord of the rings" : {"type" : "book", "year" : "1950-1974"} , "Stairway to heaven" : {"type" : "song", "year" : "1950-1974"} , "We will rock you" : {"type" : "song", "year" : "1975-1995"} , "The nightmare before Christmas" : {"type" : "movie", "year" : "1975-1995"} , "Killing me softly" : {"type" : "song", "year" : "1950-1974"} , "Blowing in the wind" : {"type" : "song", "year" : "1950-1974"} , "Girl with a pearl earring" : {"type" : "movie", "year" : "1996-2016"} , "The grapes of wrath" : {"type" : "book", "year" : "1950-1974"} , "While my guitar gently weeps" : {"type" : "song", "year" : "1950-1974"} , "Snow white and the seven dwarfs" : {"type" : "book", "year" : "earlier"} , "Somewhere over the rainbow" : {"type" : "song", "year" : "earlier"} , "House of the rising sun" : {"type" : "song", "year" : "1950-1974"} , "Somebody that I used to know" : {"type" : "song", "year" : "1996-2016"} , "Gone with the wind" : {"type" : "movie", "year" : "earlier"} , "I will survive" : {"type" : "song", "year" : "1975-1995"} , "Living on a prayer" : {"type" : "song", "year" : "1975-1995"} , "Raindrops keep fallin' on my head" : {"type" : "song", "year" : "1950-1974"} , "Everybody needs somebody to love" : {"type" : "song", "year" : "1975-1995"} , "Call me maybe" : {"type" : "song", "year" : "1996-2016"} , "The spy who shagged me" : {"type" : "movie", "year" : "1996-2016"} , "We are the champions" : {"type" : "song", "year" : "1975-1995"} , "Knocking on heaven's door" : {"type" : "song", "year" : "1950-1974"} , "A tale of two cities" : {"type" : "book", "year" : "earlier"} , "Are you lonesome tonight?" : {"type" : "song", "year" : "1950-1974"} , "The show must go on" : {"type" : "song", "year" : "1975-1995"} , "Slippery when wet" : {"type" : "song", "year" : "1975-1995"} };
		
		//when we click on a checkbox, show or hide the elements matched
		 $(':checkbox').change(function () {
      		 var $id_matcher = new RegExp("\\d+");
      		 $(".tableSorter tr").each(function () {
      			var $quoteSpan = $(this).children("td").children("span");
      		 	var $sentence_id = $quoteSpan.html();
      		 	if ($sentence_id == null) return;
      		 	$item = $quotedata[$sentence_id];
      		 	if ($item == null) return;
      		 	var $year_selector = '.year[id="'+$item["year"]+'"]';
      		 	var $type_selector = '.type[id="'+$item["type"]+'"]';
      		 	var $sort_idx = Number($(this).children("td").first().html());
      		 	//if the item does not match with both the checked properties
      		 	if ( ! ($($year_selector).is(':checked') && $($type_selector).is(':checked')) ) {
      		 	    if ($sort_idx > 0) { 
      		 	    	//change the index so to put it at the end of the table
      		 	    	$sort_idx = $sort_idx - 10000;
      		 	    	$(this).children("td").first().html($sort_idx);
      		 	    	//if the item was enabled (i.e. white => not grey) then disable it
      		 	    	//because it was hidden from the list
          		 	 	if ($quoteSpan.attr("style").indexOf("rgb(153, 153, 153)") < 0)
          		 	 		changeQuoteState($quoteSpan.attr("id"));
      		 	    }
      		 	    //also hide the element if it is still visible
      		 	    if ($(this).is(":visible"))
      		 	    	$(this).hide();
      		 		
      		 	}
      		 	else { //otherwise restore everything (except for the grayed out)
  		 	    if ($sort_idx < 0) {
  		 	    	$sort_idx = $sort_idx + 10000;
  		 	    	$(this).children("td").first().html($sort_idx);
  		 	    }
  		 	    if (!$(this).is(":visible"))
      		 		$(this).show();
  		 	}
      		});
    		$('table.tableSorter').isortope('resort'); 
       });
		
		
		function changeQuoteState(quoteID){
			var quoteIsAlreadyInArray = false;
			var firstEmptyPos = -1;

			for (var i=0; i < window.quotesRemovedArray.length; i++) {
				if (window.quotesRemovedArray[i] === "")
					firstEmptyPos = i;
				if (window.quotesRemovedArray[i] === quoteID) {
					quoteIsAlreadyInArray = true;
					window.quotesRemovedArray[i] = "";
					$("#"+quoteID).each(function () { 
						if ($(this).attr("style").indexOf("rgb(153, 153, 153)") >= 0) {
							$(this).css("color", "");
						}
						else {
							$(this).css("color", "rgb(153, 153, 153);");
						}
					})
				}
			}
			if (!quoteIsAlreadyInArray) {
				var pos = window.quotesRemovedArray.length;
				if (firstEmptyPos >= 0) {
					pos = firstEmptyPos;
				}
				window.quotesRemovedArray[pos] = quoteID;
				$("#"+quoteID).each(function () {  
						//if this was grayed out, remove the gray
						if ($(this).attr("style").indexOf("rgb(153, 153, 153)") >= 0) {
							$(this).css("color", "");
						}
						else {
							$(this).css("color", "rgb(153, 153, 153);");
						}
				})
			}
		}
		
		
		
		$("a[post=true]").each(function () {
			$(this).on('click', function () {
					var selectedIDS = []; 
					$(".quoteSpan").each(function () {  
						if ($(this).css("color").indexOf("rgb(153, 153, 153)") < 0) {
							selectedIDS[selectedIDS.length] = $(this).attr("id");
						}
					});
					doPost($(this).attr('phref'),JSON.stringify(selectedIDS));
             });
		});
		
		function doPost(url, activeQuoteIDs) {
    		    var jForm = $('<form></form>');
    		    jForm.attr("style","display:none;");
    		    jForm.attr('action', url);
    		    jForm.attr('method', 'post');
    		    var jInput1 = $("<input>");
    		    jInput1.attr('name', 'activeQuotes');
    		    jInput1.attr('value', activeQuoteIDs);
    		    jForm.append(jInput1);
    		    jForm.appendTo('body').submit();
    	};
	</script>
</body>
</html>