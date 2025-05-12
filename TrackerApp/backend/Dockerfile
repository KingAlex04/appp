FROM node:16-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install --production

COPY . .

# Create reports directory
RUN mkdir -p reports

EXPOSE 3000

CMD ["node", "index.js"] 