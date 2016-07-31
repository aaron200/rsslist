import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RssList extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {

    String[] siteArr = {"http://searchcloudapplications.techtarget.com", "http://searchsoa.techtarget.com", "http://searchsoftwarequality.techtarget.com",
        "http://searchwindevelopment.techtarget.com", "http://searchbusinessanalytics.techtarget.com", "http://searchfinancialapplications.techtarget.com",
        "http://searchcontentmanagement.techtarget.com", "http://searchcrm.techtarget.com", "http://searchdatamanagement.techtarget.com", 
        "http://searchsalesforce.techtarget.com"};
    String[] siteNameArr = {"SearchCloudApplications", "SearchSOA", "SearchSoftwareQuality", "SearchWinDevelopment", "SearchBusinessAnalytics", "SearchFinancialApplications",
        "SearchContentManagement", "SearchCRM", "SearchDataManagement", "SearchSalesForce"};

    List<String> siteList = new ArrayList<String>(Arrays.asList(siteArr));
    List<String> siteNameList = new ArrayList<String>(Arrays.asList(siteNameArr));
    Map<Date, RssItem> itemMap = new HashMap<Date,RssItem>();
    DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    for(String site : siteList) {
        Document doc = Jsoup.connect(site + "/rss").get();
        String rssLink = doc.getElementById("articleBody").select("a").first().attr("href");
        Document xml = Jsoup.connect(rssLink).get();
        Elements items = xml.select("item"); 
        for(Element item : items) {
            String title="";
            String link="";
            String description="";
            String pubDate="";
            Date date = new Date();
            for(Element descriptor : item.children()) {
                switch(descriptor.tagName()) {
                    case "title":
                        title = descriptor.text();
                        break;
                    case "link":
                        link = descriptor.text();
                        break;
                    case "description":
                        description = descriptor.text();
                        break;
                    case "pubdate":
                        pubDate = descriptor.text();
                        try {
                            date = formatter.parse(descriptor.text());
                        } catch(ParseException e) {
                            System.out.println(e);
                        }
                        break;
                    default:
                        break;
                }

            }
            if(!title.equals("") && !link.equals("") && !pubDate.equals("")) {
                RssItem rss = new RssItem(title, link, description, pubDate);
                itemMap.put(date, rss);
            }
        }
    }

    List<Date> dates = new ArrayList<Date>(itemMap.keySet());
    Collections.sort(dates, new Comparator<Date>() {
        @Override
        public int compare(Date d1, Date d2) {
            return d2.compareTo(d1);
        }
    }); 

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html>");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\" />");

        String title = "TechTarget RSS List";
        String description = "Below are the articles combined from one RSS feed from each of these pages:";
        String siteListItems = "";
        for(String siteName : siteNameList) {
            siteListItems += "<li><a href=\"" + siteList.get(siteNameList.indexOf(siteName)) + "\">" + siteName + "</a></li>";
        }
        out.println("<title>" + title + "</title>");
        out.println("<h1>" + title + "</h1>");
        out.println("</head>");

        out.println("<body bgcolor=\"white\">");
        out.println("<p>" + description + "</p>" );
        out.println("<ul style='margin-bottom:50px;'>" + siteListItems + "</ul>");
        for(Date d : dates) {
            RssItem rss = itemMap.get(d);
            out.println("<div style='width:800px; margin-bottom:50px;'>");
            out.println("<h4 style='margin-bottom:5px'><a href=\"" + rss.getLink() + "\">" + rss.getTitle() + "</a></h4>");
            out.println("<p style='margin-top:0px; color:grey'>" + rss.getPubDate() + "</p>");
            out.println("<p>" + rss.getDescription() + "</p>");
            out.println("</div>");
        } 
        out.println("</body>");

        out.println("</html>");
    }


    public class RssItem {
        private String title;
        private String link;
        private String description;
        private String pubDate;

        public RssItem(String title, String link, String description, String pubDate) {
            this.title = title;
            this.link = link;
            this.description = description;
            this.pubDate = pubDate;
        }
        public String getTitle() {
            return this.title;
        }
        public String getLink() {
            return this.link;
        }
        public String getDescription() {
            return this.description;
        }
        public String getPubDate() {
            return this.pubDate;
        }

    }

}



