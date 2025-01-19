package com.muitilingualism.zip;

import java.nio.file.Path;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Zip {
	public static void main(String... args) {
		try {
			System.out.println("Zipping " + args[0]);
			Path filePath = Path.of(args[0]);
			System.out.println("File name: " + filePath.getFileName());
			System.out.println("File root: " + filePath.getRoot());
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Missing input file to be zipped");
		}
	}
}

class LocalFileHeader {
	public final byte[] SIGNATURE = new byte[] { 0x50, 0x4b, 0x03, 0x04}; //0x04034b50

	private int size = 30;

	private byte[] signature = SIGNATURE;
	private byte[] versionNeeded;
	private byte[] flags;
	private byte[] compressionMethod;
	private byte[] lastModTime;
	private byte[] lastModDate;
	private byte[] crc32;
	private byte[] compressedSize;
	private byte[] uncompressedSize;
	private byte[] filenameLength;
	private byte[] extraFieldLength;
	private byte[] filename;
	private byte[] extraField;

	public LocalFileHeader(
			byte[] signature,
			byte[] versionNeeded,
			byte[] flags,
			byte[] compressionMethod,
			byte[] lastModTime,
			byte[] lastModDate,
			byte[] crc32,
			byte[] compressedSize,
			byte[] uncompressedSize,
			byte[] filenameLength,
			byte[] extraFieldLength,
			byte[] filename,
			byte[] extraField) {
		assert signature.length         == 4;
		assert versionNeeded.length     == 2;
		assert flags.length             == 2;
		assert compressionMethod.length == 2;
		assert lastModTime.length       == 2;
		assert lastModDate.length       == 2;
		assert crc32.length             == 4;
		assert compressedSize.length    == 4;
		assert uncompressedSize.length  == 4;
		assert filenameLength.length    == 2;
		assert extraFieldLength.length  == 2;

		int filenameLen = ByteLib.toInt(filenameLength);
		assert filename.length == filenameLen;

		int extraFieldLen = ByteLib.toInt(extraFieldLength);
		assert extraField.length == extraFieldLen;

		this.size = this.size + filenameLen + extraFieldLen;

		this.signature = signature;
		this.versionNeeded = versionNeeded;
		this.flags = flags;
		this.compressionMethod = compressionMethod;
		this.lastModTime = lastModTime;
		this.lastModDate = lastModDate;
		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;
		this.filenameLength = filenameLength;
		this.extraFieldLength = extraFieldLength;
	}

	public byte[] get() {
		byte[] ba = ByteBuffer.allocate(size)
			.put(this.signature)
			.put(this.versionNeeded)
			.put(this.flags)
			.put(this.compressionMethod)
			.put(this.lastModTime)
			.put(this.lastModDate)
			.put(this.crc32)
			.put(this.compressedSize)
			.put(this.uncompressedSize)
			.put(this.filenameLength)
			.put(this.extraFieldLength)
			.array();

		assert ba.length == this.size;
		return ba;
	}
};

class FileData {
	private byte[] data;

	private int size;

	public FileData(byte[] data) {
		this.size = data.length;
		this.data = data;
	}
}

class DataDescriptor {
	public static final byte[] SIGNATURE = new byte[] { 0x50, 0x4b, 0x07, 0x08 }; //0x08074b50

	private int size = 12;

	private byte[] signature = null; // optional
	private byte[] crc32;
	private byte[] compressedSize;
	private byte[] uncompressedSize;

	public DataDescriptor(
			byte[] signature,
			byte[] crc32,
			byte[] compressedSize,
			byte[] uncompressedSize) {
		assert signature == SIGNATURE;
		assert signature.length == 4;
		assert crc32.length     == 4;
		assert compressedSize.length   == 4 || compressedSize.length == 8; //8 bytes in ZIP64 format
		assert uncompressedSize.length == 4 || compressedSize.length == 8; //8 bytes in ZIP64 format

		this.signature = signature;
		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;
		this.size = 8 + compressedSize.length + uncompressedSize.length;
	}

	public DataDescriptor(
			byte[] crc32,
			byte[] compressedSize,
			byte[] uncompressedSize) {
		assert crc32.length == 4;
		assert compressedSize.length   == 4 || compressedSize.length == 8; //8 bytes in ZIP64 format
		assert uncompressedSize.length == 4 || compressedSize.length == 8; //8 bytes in ZIP64 format

		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;

		this.size = 4 + compressedSize.length + uncompressedSize.length;
	}

	public byte[] get() {
		if (this.signature == null) {
			byte[] ba = ByteBuffer.allocate(size)
				.put(this.crc32)
				.put(this.compressedSize)
				.put(this.uncompressedSize)
				.array();
			assert ba.length == this.size;
			return ba;
		} else {
			byte[] ba = ByteBuffer.allocate(size)
				.put(this.signature)
				.put(this.crc32)
				.put(this.compressedSize)
				.put(this.uncompressedSize)
				.array();
			assert ba.length == this.size;
			return ba;
		}
	}
}

class CentralDirectoryFileHeader {
	public static final byte[] SIGNATURE = new byte[] {0x50, 0x4b, 0x01, 0x02}; //0x02014b50

	private int size = 46;

	private byte[] signature = SIGNATURE;
	private byte[] versionMadeBy;
	private byte[] versionNeeded;
	private byte[] flags;
	private byte[] compressionMethod;
	private byte[] lastModTime;
	private byte[] lastModDate;
	private byte[] crc32;
	private byte[] compressedSize;
	private byte[] uncompressedSize;
	private byte[] filenameLength;
	private byte[] extraFieldLength;
	private byte[] fileCommentLength;
	private byte[] diskNumberStart;
	private byte[] internalFileAttributes;
	private byte[] externalFileAttributes;
	private byte[] relativeOffsetOfLocalHeader;
	private byte[] filename;
	private byte[] extraField;
	private byte[] fileComment;

	public CentralDirectoryFileHeader(
			byte[] signature,
			byte[] versionMadeBy,
			byte[] versionNeeded,
			byte[] flags,
			byte[] compressionMethod,
			byte[] lastModTime,
			byte[] lastModDate,
			byte[] crc32,
			byte[] compressedSize,
			byte[] uncompressedSize,
			byte[] filenameLength,
			byte[] extraFieldLength,
			byte[] fileCommentLength,
			byte[] diskNumberStart,
			byte[] internalFileAttributes,
			byte[] externalFileAttributes,
			byte[] relativeOffsetOfLocalHeader,
			byte[] filename,
			byte[] extraField,
			byte[] fileComment) {
		assert signature.length          == 4;
		assert versionMadeBy.length      == 2;
		assert versionNeeded.length      == 2;
		assert flags.length              == 2;
		assert compressionMethod.length  == 2;
		assert lastModTime.length        == 2;
		assert lastModDate.length        == 2;
		assert crc32.length              == 4;
		assert compressedSize.length     == 4;
		assert uncompressedSize.length   == 4;
		assert filenameLength.length     == 2;
		assert extraFieldLength.length   == 2;
		assert fileCommentLength.length  == 2;
		assert diskNumberStart.length    == 2;
		assert internalFileAttributes.length == 2;
		assert externalFileAttributes.length == 4;
		assert relativeOffsetOfLocalHeader.length == 4;

		int filenameLen = ByteLib.toInt(filenameLength);
		assert filename.length == filenameLen;

		int extraFieldLen = ByteLib.toInt(extraFieldLength);
		assert extraField.length == extraFieldLen;

		int fileCommentLen = ByteLib.toInt(fileCommentLength);
		assert fileComment.length == fileCommentLen;

		this.size = this.size + filenameLen + extraFieldLen + fileCommentLen;

		this.signature = signature;
		this.versionMadeBy = versionMadeBy;
		this.versionNeeded = versionNeeded;
		this.flags = flags;
		this.compressionMethod = compressionMethod;
		this.lastModTime = lastModTime;
		this.lastModDate = lastModDate;
		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;
		this.filenameLength = filenameLength;
		this.extraFieldLength = extraFieldLength;
		this.fileCommentLength = fileCommentLength;
		this.diskNumberStart = diskNumberStart;
		this.internalFileAttributes = internalFileAttributes;
		this.externalFileAttributes = externalFileAttributes;
		this.relativeOffsetOfLocalHeader = relativeOffsetOfLocalHeader;
		this.filename = filename;
		this.extraField = extraField;
		this.fileComment = fileComment;
	}

	public byte[] get() {
		byte[] ba =  ByteBuffer.allocate(size)
			.put(this.signature)
			.put(this.versionMadeBy)
			.put(this.versionNeeded)
			.put(this.flags)
			.put(this.compressionMethod)
			.put(this.lastModTime)
			.put(this.lastModDate)
			.put(this.crc32)
			.put(this.compressedSize)
			.put(this.uncompressedSize)
			.put(this.filenameLength)
			.put(this.extraFieldLength)
			.put(this.fileCommentLength)
			.put(this.diskNumberStart)
			.put(this.internalFileAttributes)
			.put(this.externalFileAttributes)
			.put(this.relativeOffsetOfLocalHeader)
			.put(this.filename)
			.put(this.extraField)
			.put(this.fileComment)
			.array();

		assert ba.length == this.size;
		return ba;
	}
}

class EndOfCentralDirectoryRecord {
	public static final byte[] SIGNATURE = new byte[] { 0x50, 0x4b, 0x05, 0x06 }; //0x06054b50
	private byte[] signature = SIGNATURE;
}

class ByteLib {

	// for inverse use Byte.valueOf(byte b)
	public static byte[] toPrimitive(Byte[] ba) {
		byte[] bytes = new byte[ba.length];
		int i=0;
		for (Byte b: ba)
			bytes[i++] = b.byteValue();
		return bytes;
	}

	public static int toInt(byte[] ba) {
		return new BigInteger(ba).intValue();
	}
}
