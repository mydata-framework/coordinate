# coordinate
## 无单点微服务架构的分布式协调服务

### 直接部署到TOMCAT启动之后，访问 /login进行登录，查看服务列表



### Nginx WebSocket 默认设置了激进的 60 秒超时

```
 location /websocket {
  proxy_pass http://backend;
  proxy_http_version 1.1;
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection "upgrade";
  proxy_read_timeout 3600; 
  proxy_send_timeout 3600; 
}
```
####  proxy_read_timeout  设置两次读操作间的超时为 60 分钟
#### proxy_send_timeout  设置两次写操作间的超时为 60 分钟
