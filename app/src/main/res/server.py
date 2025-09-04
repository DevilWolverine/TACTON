import asyncio
import json
import websockets

# Estado en memoria
global markers
users = {}   # Usuario y sus datos
markers = [] # Lista de marcadores

connected_clients = set()

async def handler(websocket):
	print("Nuevo cliente conentado")
	connected_clients.add(websocket)

	# Estado inicial
	await websocket.send(json.dumps({
		"type": "init_state",
		"users": users,
		"markers": markers
	}))

	try:
		async for message in websocket:
			try:
				data = json.loads(message)
				print("Recidibo:", data)
			except Exception as e:
				print("Error parseando JSON:", e, message)
				continue

			msg_type = data.get("type")

			if msg_type == "hello":
				user = data.get("user")
				print(f"Usuario conectado: {user}")
				users[user] = {
					"id": len(users)+1,
					"name": user,
					"point": None,
					"bearing": 0
				}
				websocket.current_user = user

			elif msg_type == "position":
				user = data.get("user")
				users[user] = {
					"id": data.get("id"),
					"point": data.get("point"),
					"bearing": data.get("bearing",0)
				}
				await broadcast(data, websocket)

			elif msg_type == "create":
				marker = {
					"id": len(markers) + 1,
					"user": data.get("user"),
					"icon": data.get("icon"),
					"option": data.get("option"),
					"marker": data.get("marker"),
					"medevac": data.get("medevac"),
					"tutela": data.get("tutela"),
				}
				markers.append(marker)
				await broadcast({"type": "create", **marker}, websocket)

			elif msg_type == "delete":
				marker_id = data.get("id")
				point = data.get("point")
				before = len(markers)
				markers[:] = [i for i in markers if i.get("id") != marker_id]
				print(len(markers))
				after = len(markers)

				print(f"Borrado marcador {marker_id}, total: {before} -> {after}")
				print(len(markers))
				await broadcast({"type": "delete", "point": point}, websocket)
	finally:
		print(len(connected_clients))
		connected_clients.remove(websocket)
		current_user = getattr(websocket, "current_user", None)
		print(len(connected_clients))
		if current_user and current_user in users:
			await broadcast({"type": "user_disconnect", "user": current_user})
			del users[current_user]
		print("Cliente desconectado")

# Mensaje Broadcast
async def broadcast(message, sender=None):
	msg = json.dumps(message)
	disconnected = []
	for client in connected_clients:
		if client != sender:
			try:
				await client.send(msg)
			except Exception as e:
				print(" Error enviando a cliente: {e}")
				disconnected.append(client)
		for client in disconnected:
			connected_clients.remove(client)
			user = getattr(client, "current_user", None)
			if user and user in users:
				del users[user]
				await broadcast({"type": "user_disconnect", "user":user})
				print(connected_clients)
		print("Cliente {client} desconectado.")

async def main():
	async with websockets.serve(handler, "0.0.0.0", 8080):
		print("Servidor WebSocket corriendo en ws://192.168.1.32:8080")
		await asyncio.Future() #run forever

if __name__ == "__main__":
	asyncio.run(main())