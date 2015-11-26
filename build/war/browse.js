var serviceUrl = "browse";

function search() {
  var text = $("#query").attr("value");
  $("#navi").css("visibility", "hidden");
  $.ajax({
    type: "POST",
    url: serviceUrl,
    data: {query: text, type: "search"},
    dataType: "json",
    success: function(data, status) {
          var resultHtml = "<ul>";
          if (data.status == "ok") {
            for (var i = 0; i < data.docs.length; i++) {
              resultHtml += "<li><a href=\"#\" onclick='showDoc(\"" + data.docs[i] + "\")'>";
              resultHtml += data.docs[i];
              resultHtml += "</a></li>";
            }
          }
          resultHtml += "</ul>";
          $("#tags").html("");
          $("#content").html(resultHtml);
        }
   });
}

function showDoc(docid) {
  $("#navi").css("visibility", "visible");
  $("#docid").attr("value", docid);
  $.getJSON(serviceUrl, {type: "doc", docid: docid},
        function(data, status) {
          $("#tags").html(data.tags);
          $("#content").html(data.content);
        });
}

function previous(e) {
  e.preventDefault();
  var currentId = $("#docid").attr("value");
  var newid = parseInt(currentId) - 1;
  if (newid >= 0)
    showDoc("" + newid);
}

function next(e) {
  e.preventDefault();
  var currentId = $("#docid").attr("value");
  var newid = parseInt(currentId) + 1;
  showDoc("" + newid);
}

$(document).ready(function() {
  $("#querybtn").click(search);
  $("#previous").click(previous);
  $("#next").click(next);
})
