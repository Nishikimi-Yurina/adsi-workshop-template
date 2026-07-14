/** @type {import('next').NextConfig} */
const isSagemaker = process.env.SAGEMAKER === '1';
const basePath = isSagemaker ? '/codeeditor/default/absports/3000' : '';

const nextConfig = {
  basePath,
  skipTrailingSlashRedirect: isSagemaker,
  env: {
    NEXT_PUBLIC_API_BASE: basePath,
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
};

module.exports = nextConfig;
