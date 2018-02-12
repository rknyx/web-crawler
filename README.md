# Web-crawler

Small multithreaded web-crawler which parses single chosen website.

## Main features:
 - Utilizes level synchronous parallel breadth-first search traversal
 - Uses thread pool with thread count = double processors count by default
 - Works with HTTP and HTTPS schemes. HTTP is more preferable if both are met on the page
 - Pre-filters urls by extensions like (.csv, .xml) to remain only document-like
 - Filters documents by document-type header. Accepts only text/html.
 - Allows to set timeout before request to crawl websites politely
 - Does not parse external links (subdomains also).

## How to use:
Create WebCrawler instance with seed url and desired scan depth. Zero depth corresponds to parsing single seed url. Depth=1 will scan one additional level.  Resulted sorted list of frequencies contains top 100 most frequent words.

	URL url = new URL("https://en.wikipedia.org/wiki/Main_Page");
	WebCrawler crawler = new WebCrawler(url, 1);
	List<Frequency> frequencyList = crawler.scanTop100Words();

There is an advanced constructor of WebCrawler if there is a need to use custom thread count, traversal or set politeness.

	public WebCrawler(URL url, int scanDepth, ParsingStrategy parsingStrategy, int threadCount)

## Ways to improve and limitations:
There are several additional features that can improve WebCrawler:

- Use fixed point parallel breadth-first search traversal. This approach promises better performance: synchronization will not stuck on slow web pages. But the implementation is a bit more complex and should be done carefully to avoid or minimize repeated parsing. Level synchronous algorithm seems reasonable for simple crawler since a typical web page contains a lot of links (much more than thread count) and synchronizations are rare. 
- Consider stream parsers instead of JSoup since it loads the entire document body in memory and parses DOM.
- Add setting to switch subdomain support.
- Process responses more carefully. E.x: adapt politeness when get 429 (Too Many Requests).
- Support user-agent customization.
- (For better result quality) Support language model to match different word forms as one word like "keep, kept, keeps, keeping". Requires a separate model for each language.
- (For very large-scaled crawling) keep results in Trie to save memory and merge results more efficient.
- (For very large-scaled crawling) persistence of Crawler state.