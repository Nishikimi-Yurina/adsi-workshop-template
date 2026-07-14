import http from 'node:http';

const LISTEN_PORT = 3000;
const TARGET_PORT = 3001;
const PREFIX = '/codeeditor/default';

const server = http.createServer((req, res) => {
  const targetPath = PREFIX + req.url;
  console.log(`${req.method} ${req.url} -> ${targetPath}`);

  const options = {
    hostname: '127.0.0.1',
    port: TARGET_PORT,
    path: targetPath,
    method: req.method,
    headers: req.headers,
  };

  const proxyReq = http.request(options, (proxyRes) => {
    res.writeHead(proxyRes.statusCode, proxyRes.headers);
    proxyRes.pipe(res);
  });

  proxyReq.on('error', (err) => {
    console.error('Proxy error:', err.message);
    res.writeHead(502);
    res.end('Bad Gateway');
  });

  req.pipe(proxyReq);
});

server.listen(LISTEN_PORT, '0.0.0.0', () => {
  console.log(`SageMaker proxy: :${LISTEN_PORT} -> :${TARGET_PORT} (prefix ${PREFIX})`);
});
