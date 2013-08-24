package com.yammer.metrics.reporting;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;

public class CustomFormatReporter extends GraphiteReporter {

  private static final Logger LOG = LoggerFactory.getLogger(CustomFormatReporter.class);
  
  public static final String HOSTNAME = "$hostname";
  public static final String METRICS_GROUP = "$metricsgroup";
  public static final String METRICS_TYPE = "$metricstype";
  public static final String METRICS_NAME = "$metricsname";
  public static final String METRICS_SCOPE = "$metricsscope";
  public static final String METRICS_MBEAN = "$metricsmbeanname";
  
  private String format;
  private String hostname;
  
  public CustomFormatReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, SocketProvider socketProvider, Clock c,String format) throws IOException{
    super(metricsRegistry, prefix, predicate, socketProvider, c);
    this.format = format;
    hostname = java.net.InetAddress.getLocalHost().getHostName();
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
    hostname = java.net.InetAddress.getLocalHost().getHostName();
  }
  
  public static void replace(StringBuilder sb, String toFind, String value){
    int index = sb.indexOf(toFind);
    if (index > -1){
      sb.replace(index, index+toFind.length() , value);
    }
  }
  
  @Override
  protected String sanitizeName(MetricName name) {
    StringBuilder sb = new StringBuilder(format);
    replace(sb, HOSTNAME, hostname);
    replace(sb, METRICS_GROUP, name.getGroup());
    replace(sb, METRICS_TYPE, name.getType());
    replace(sb, METRICS_NAME, name.getName());
    replace(sb, METRICS_MBEAN, name.getMBeanName());
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
  
  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

}
