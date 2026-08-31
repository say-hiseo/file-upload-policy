import 'dotenv/config';
import express from 'express';
import path from 'path';
import { createServer as createViteServer } from 'vite';

const app = express();
const PORT = 3000;

// No body-parsing middleware here: /api/* requests are forwarded untouched to the
// real backend via vite.config.ts's dev proxy, and parsing the body here would
// consume the request stream before the proxy can forward it.

// ----------------------------------------------------
// Vite Dev Server / Static Asset Handler
// ----------------------------------------------------

async function startServer() {
  if (process.env.NODE_ENV !== 'production') {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), 'dist');
    app.use(express.static(distPath));
    app.get('*', (_req, res) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`[File Policy Manager] Server running on port ${PORT}`);
  });
}

startServer();
