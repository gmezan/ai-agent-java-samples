import { ChatAppRequest } from "./models";

const BACKEND_URI = import.meta.env.VITE_BACKEND_URI ? import.meta.env.VITE_BACKEND_URI : "";

function getHeaders(stream:boolean): Record<string, string> {
    var headers: Record<string, string> = {
        "Content-Type": "application/json"
    };

    if (stream) {
        headers["Accept"] = "application/x-ndjson";
    } else {
        headers["Accept"] = "application/json";
    }

    return headers;
}

export async function chatApi(request: ChatAppRequest): Promise<Response> {
    return await fetch(`${BACKEND_URI}/chat`, {
        method: "POST",
        headers: getHeaders(request.stream || false),
        body: JSON.stringify(request)
    });
}

export function getCitationFilePath(citation: string): string {
    return `${BACKEND_URI}/content/${citation}`;
}
