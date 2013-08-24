package com.yammer.metrics.reporting;

import java.io.IOException;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;

public class CustomFormatReporter extends GraphiteReporter {

  private static final Logger LOG = LoggerFactory.getLogger(CustomFormatReporter.class);
  private String format;
  private String hostname;
  
  
  public CustomFormatReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, SocketProvider socketProvider, Clock c,String format) throws IOException{
    super(metricsRegistry, prefix, predicate, socketProvider, c);
    this.format = format;
    hostname = "localhost";// TODO derive this locally
  }
  /**
   * 
   * @param metricsRegistry
   * @param prefix
   * @param predicate
   * @param socketProvider
   * @param format
   * @throws IOException
   */
  public CustomFormatReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, SocketProvider socketProvider, String format) throws IOException{
    super(metricsRegistry, prefix, predicate, socketProvider, Clock.defaultClock());
    this.format = format;
    hostname = "localhost";// TODO derive this locally
  }
  /*
  public CustomFormatReporter(MetricsRegistry metricsRegistry, String host, int port, String prefix, String format)
          throws IOException {
    
    super(metricsRegistry, host, port, prefix);
    this.format = format;
    hostname = "localhost";//TODO derive this locally
  }*/
  
  void replace(StringBuilder sb, String toFind, String value){
    int index = sb.indexOf(toFind);
    if (index > -1){
      sb.replace(index, index+toFind.length() , value);
    }
  }
  
  @Override
  protected String sanitizeName(MetricName name) {
    StringBuilder sb = new StringBuilder(format);
    replace(sb, "$hostname", hostname);
    replace(sb, "$metricsgroup", name.getGroup());
    replace(sb, "$metricstype", name.getType());
    replace(sb, "$metricsname", name.getName());
    replace(sb, "$metricsmbeanname", name.getMBeanName());
    if (name.hasScope()){
      replace(sb, "$metricsscope", name.getScope());
    } else {
      replace(sb, ".$metricsscope", "");
    }
    return sb.toString();
  }
  
  @Override
  protected void sendToGraphite(long timestamp, String name, String value) {
    try {
        if (!prefix.isEmpty()) {
            writer.write(prefix);
        }
        writer.write(sanitizeString(name));
        writer.write('.');
        writer.write(value);
        writer.write(' ');
        writer.write(Long.toString(timestamp));
        writer.write('\n');
        writer.flush();
    } catch (IOException e) {
        LOG.error("Error sending to Graphite:", e);
    }
}

}
