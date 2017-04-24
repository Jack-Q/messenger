export const checkPacket = (buffer, low, high) => {
  let valid, complete;
  return { valid, complete };
}

export const readPacket = (buffer, low, high) => {
  let length = 0, type, payload;
  return { data: { type, payload }, length };
}