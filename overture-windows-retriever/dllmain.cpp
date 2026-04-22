// i've never programmed in C++
// ever

// please forgive me
#include "pch.h"

#include <winrt/Windows.Media.Control.h>
#include <winrt/Windows.Storage.Streams.h>
#include <winrt/Windows.Foundation.h>

#include <iostream>
#include <string>

#include <cstdio>

using namespace winrt;
using namespace Windows::Media::Control;

struct Result {
    const char* name;
    const char* artist;
    const char* album;
    uint8_t* thumbnailData;
    int32_t thumbnailSize;
    int64_t duration;
    int64_t current;
    int64_t timestamp;
};

const char* ToDuplicatedString(winrt::hstring str) 
{
    auto string = winrt::to_string(str);
    return _strdup(string.c_str());
}

using namespace winrt::Windows::Storage::Streams;

// something something windows epoch != unix epoch
static constexpr int64_t EPOCH_DIFF_MS = 11644473600000LL;

// TODO: is there a memory leak here???
extern "C" __declspec(dllexport) Result* Retrieve()
{
    auto manager = GlobalSystemMediaTransportControlsSessionManager::RequestAsync().get();
    auto currentSession = manager.GetCurrentSession();

    if (!currentSession) return NULL;

    auto playbackInfo = currentSession.GetPlaybackInfo();
    if (!playbackInfo || playbackInfo.PlaybackStatus() != GlobalSystemMediaTransportControlsSessionPlaybackStatus::Playing) return NULL;

    auto properties = currentSession.TryGetMediaPropertiesAsync().get();
    if (!properties) return NULL;

    auto timeline = currentSession.GetTimelineProperties();
    Result* ptr = new Result;

    ptr->name = ToDuplicatedString(properties.Title());
    ptr->artist = ToDuplicatedString(properties.Artist());
    ptr->album = ToDuplicatedString(properties.AlbumTitle());

    auto stream = properties.Thumbnail().OpenReadAsync().get();
    uint32_t size = static_cast<uint32_t>(stream.Size());
    ptr->thumbnailSize = size;
       
    uint8_t* buffer = new uint8_t[size];
    DataReader reader(stream);
    reader.LoadAsync(size).get();
    reader.ReadBytes({ buffer, size });
    reader.DetachStream();
    ptr->thumbnailData = buffer;

    ptr->duration = (timeline.EndTime().count() - timeline.StartTime().count()) / 10000;
    ptr->current = (timeline.Position().count() / 10000);
    ptr->timestamp = (timeline.LastUpdatedTime().time_since_epoch().count() / 10000) - EPOCH_DIFF_MS;

    return ptr;
}