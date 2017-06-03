// Configuration of server

// TCP port of service (UDP port is selected randomly)
export const port = 12121;

// Public accessible host
export const host = '0.0.0.0';

// buffer size for packet processing, ought to be at least 2x of maximum packet size
export const BUFFER_SIZE = 10240;

// server name for service status report
export const SERVER_NAME = 'Default Localhost Server';
