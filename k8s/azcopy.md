### Tried azcopy 

Downloading the data from s3 and unpacking can be slow; It seems like azcopy would work.

NOTE: I wasn't able to get this to work.


```
azcopy copy 'https://fe7448eb9513d4d66be120b.file.core.windows.net/pvc-1176e15b-e5dc-4947-8c8d-160c994ad9ac?sv=2022-11-02&ss=f&srt=sco&sp=rwdlc&se=2023-09-27T09:11:50Z&st=2023-09-27T01:11:50Z&sip=74.128.81.22&spr=https&sig=k64dF9WTpo3jJIA0SvTW3tqPHnJipNYaPU%2BWFWNX%2BrQ%3D' 'https://f9a7319f992094f338d72bb.file.core.windows.net/pvc-39302df8-554a-4e40-b701-5abdf3cbec0b?sv=2022-11-02&ss=f&srt=sco&sp=rwdlc&se=2023-09-27T09:09:31Z&st=2023-09-27T01:09:31Z&sip=74.128.81.22&spr=https&sig=RCYb%2BgA3RSt6BR9IF6Alodi8ZSIyYFYXAAv1enEq2%2BQ%3D' --recursive --preserve-smb-permissions=true --preserve-smb-info=true
```

Tried a few variations.  The output showed errors and only some of the files appeared on new PVC and even the ons that appeared were not function head/tail would output nothing. 

From

FileEndpoint=https://fe7448eb9513d4d66be120b.file.core.windows.net/;SharedAccessSignature=sv=2022-11-02&ss=f&srt=sco&sp=rwdlc&se=2023-10-28T08:47:57Z&st=2023-09-28T00:47:57Z&sip=74.128.81.22&spr=https&sig=uk3AYaxqMka0ftKmXeuAKUx18J8Ngx5Dac25vyMuah0%3D

?sv=2022-11-02&ss=f&srt=sco&sp=rwdlc&se=2023-10-28T08:47:57Z&st=2023-09-28T00:47:57Z&sip=74.128.81.22&spr=https&sig=uk3AYaxqMka0ftKmXeuAKUx18J8Ngx5Dac25vyMuah0%3D

https://fe7448eb9513d4d66be120b.file.core.windows.net/?sv=2022-11-02&ss=f&srt=sco&sp=rwdlc&se=2023-10-28T08:47:57Z&st=2023-09-28T00:47:57Z&sip=74.128.81.22&spr=https&sig=uk3AYaxqMka0ftKmXeuAKUx18J8Ngx5Dac25vyMuah0%3D

To

https://f9a7319f992094f338d72bb.file.core.windows.net/pvc-39302df8-554a-4e40-b701-5abdf3cbec0b

FileEndpoint=https://f9a7319f992094f338d72bb.file.core.windows.net/;SharedAccessSignature=sv=2022-11-02&ss=f&srt=sco&sp=rwdlc&se=2023-10-28T08:45:17Z&st=2023-09-28T00:45:17Z&sip=74.128.81.22&spr=https&sig=C1lroEZd%2FZKJonuE7EXNn8HWEDS%2FAH2oz2exaGp8C6E%3D

?sv=2022-11-02&ss=f&srt=sco&sp=rwdlc&se=2023-10-28T08:45:17Z&st=2023-09-28T00:45:17Z&sip=74.128.81.22&spr=https&sig=C1lroEZd%2FZKJonuE7EXNn8HWEDS%2FAH2oz2exaGp8C6E%3D


https://learn.microsoft.com/en-us/azure/storage/common/storage-use-azcopy-files#copy-files-between-storage-accounts

azcopy copy 'https://<source-storage-account-name>.file.core.windows.net/<file-share-name>/<file-path><SAS-token>' 'https://<destination-storage-account-name>.file.core.windows.net/<file-share-name>/<file-path><SAS-token>'


azcopy copy 'https://mysourceaccount.file.core.windows.net/mycontainer/myTextFile.txt?sv=2018-03-28&ss=bfqt&srt=sco&sp=rwdlacup&se=2019-07-04T05:30:08Z&st=2019-07-03T21:30:08Z&spr=https&sig=CAfhgnc9gdGktvB=ska7bAiqIddM845yiyFwdMH481QA8%3D' 'https://mydestinationaccount.file.core.windows.net/mycontainer/myTextFile.txt?sv=2018-03-28&ss=bfqt&srt=sco&sp=rwdlacup&se=2019-07-04T05:30:08Z&st=2019-07-03T21:30:08Z&spr=https&sig=CAfhgnc9gdGktvB=ska7bAiqIddM845yiyFwdMH481QA8%3D' --preserve-smb-permissions=true --preserve-smb-info=true


azcopy copy "https://fe7448eb9513d4d66be120b.file.core.windows.net/pvc-1176e15b-e5dc-4947-8c8d-160c994ad9ac/planes?sv=2022-11-02&ss=f&srt=sco&sp=rwdlc&se=2023-10-28T08:47:57Z&st=2023-09-28T00:47:57Z&sip=74.128.81.22&spr=https&sig=uk3AYaxqMka0ftKmXeuAKUx18J8Ngx5Dac25vyMuah0%3D" "https://f9a7319f992094f338d72bb.file.core.windows.net/pvc-39302df8-554a-4e40-b701-5abdf3cbec0b?sv=2022-11-02&ss=f&srt=sco&sp=rwdlc&se=2023-10-28T08:45:17Z&st=2023-09-28T00:45:17Z&sip=74.128.81.22&spr=https&sig=C1lroEZd%2FZKJonuE7EXNn8HWEDS%2FAH2oz2exaGp8C6E%3D" --recursive --preserve-smb-permissions=true --preserve-smb-info=true 

```
INFO: Scanning...
INFO: Any empty folders will be processed, because source and destination both support folders. For the same reason, properties and permissions defined on folders will be processed
INFO: Trying 4 concurrent connections (initial starting point)

Job 380e6de5-6588-df47-7a13-a12e68f9a7d2 has started
Log file is located at: /Users/davi5017/.azcopy/380e6de5-6588-df47-7a13-a12e68f9a7d2.log

INFO: Authentication failed, it is either not correct, or expired, or does not have the correct permission -> github.com/Azure/azure-storage-file-go/azfile.newStorageError, github.com/Azure/azure-storage-file-go@v0.6.1-0.20201111053559-3c1754dc00a5/azfile/zc_storage_error.go:42
===== RESPONSE ERROR (ServiceCode=CannotVerifyCopySource) =====
Description=This request is not authorized to perform this operation.
RequestId:fc23f241-201a-0106-49a6-f1eb47000000
Time:2023-09-28T00:55:06.7063459Z, Details: 
   Code: CannotVerifyCopySource
   PUT https://f9a7319f992094f338d72bb.file.core.windows.net/pvc-39302df8-554a-4e40-b701-5abdf3cbec0b/planes/planes70002?comp=range&se=2023-10-28t08%3A45%3A17z&sig=-REDACTED-&sip=74.128.81.22&sp=rwdlc&spr=https&srt=sco&ss=f&st=2023-09-28t00%3A45%3A17z&sv=2022-11-02&timeout=901
   Content-Length: [0]
   User-Agent: [AzCopy/10.20.1 Azure-Storage/0.8.0 (go1.20.7; darwin)]
   X-Ms-Allow-Trailing-Dot: [true]
   X-Ms-Client-Request-Id: [6721b5bf-2be7-448f-7e56-1a357c00cdd4]
   X-Ms-Copy-Source: [https://fe7448eb9513d4d66be120b.file.core.windows.net/pvc-1176e15b-e5dc-4947-8c8d-160c994ad9ac/planes/planes70002?se=2023-10-28t08%3A47%3A57z&sig=-REDACTED-&sip=74.128.81.22&sp=rwdlc&spr=https&srt=sco&ss=f&st=2023-09-28t00%3A47%3A57z&sv=2022-11-02]
   X-Ms-Range: [bytes=8388608-12582911]
   X-Ms-Source-Allow-Trailing-Dot: [true]
   X-Ms-Source-Range: [bytes=8388608-12582911]
   X-Ms-Version: [2022-11-02]
   X-Ms-Write: [update]
   --------------------------------------------------------------------------------
   RESPONSE Status: 403 This request is not authorized to perform this operation.
   Content-Length: [248]
   Content-Type: [application/xml]
   Date: [Thu, 28 Sep 2023 00:55:06 GMT]
   Server: [Windows-Azure-File/1.0 Microsoft-HTTPAPI/2.0]
   X-Ms-Client-Request-Id: [6721b5bf-2be7-448f-7e56-1a357c00cdd4]
   X-Ms-Error-Code: [CannotVerifyCopySource]
   X-Ms-Request-Id: [fc23f241-201a-0106-49a6-f1eb47000000]
   X-Ms-Version: [2022-11-02]



0.0 %, 1 Done, 0 Failed, 80 Pending, 0 Skipped, 81 Total, 2-sec Throughput (Mb/s): 117.4251


Job 380e6de5-6588-df47-7a13-a12e68f9a7d2 summary
Elapsed Time (Minutes): 0.0667
Number of File Transfers: 80
Number of Folder Property Transfers: 1
Number of Symlink Transfers: 0
Total Number of Transfers: 81
Number of File Transfers Completed: 0
Number of Folder Transfers Completed: 1
Number of File Transfers Failed: 0
Number of Folder Transfers Failed: 0
Number of File Transfers Skipped: 0
Number of Folder Transfers Skipped: 0
TotalBytesTransferred: 0
Final Job Status: Cancelled
```