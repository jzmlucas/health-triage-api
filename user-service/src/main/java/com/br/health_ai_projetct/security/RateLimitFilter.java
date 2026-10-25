package com.br.health_ai_projetct.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitFilter extends OncePerRequestFilter {
 private record Bucket(long startedAt, AtomicInteger count) {}
 private final ConcurrentHashMap<String,Bucket> buckets=new ConcurrentHashMap<>();
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String ip=req.getRemoteAddr(); String route=req.getRequestURI().startsWith("/auth/")?"auth:":"api:"; String key=route+ip; long now=System.currentTimeMillis();
  if(buckets.size()>20000)buckets.entrySet().removeIf(e->now-e.getValue().startedAt()>120000);
  Bucket b=buckets.compute(key,(k,old)->old==null||now-old.startedAt()>=60000?new Bucket(now,new AtomicInteger()):old);
  int limit=route.equals("auth:")?10:120;
  int count = b.count().incrementAndGet();
  res.setHeader("X-RateLimit-Limit",Integer.toString(limit)); res.setHeader("X-RateLimit-Remaining",Integer.toString(Math.max(0,limit-count)));
  if(count>limit){res.setStatus(429);res.setHeader("Retry-After","60");res.setContentType("application/json");res.getWriter().write("{\"error\":\"rate_limit_exceeded\"}");return;} chain.doFilter(req,res);
 }
}


